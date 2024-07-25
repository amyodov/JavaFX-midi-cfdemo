package com.amyodov.midi.cfdemo;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {

    private ExecutorService midiExecutor;

    private FXMLController fxmlController;

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("MainApp.start");
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("scene.fxml"));
        final Parent root = loader.load();

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("JavaFX %s/JDK %s".formatted(
                System.getProperty("javafx.version"),
                System.getProperty("java.version")));
        stage.setScene(scene);
        stage.show();

        fxmlController = loader.getController();

        // Initialize the MIDI setup in a separate max-priority thread
        midiExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setPriority(Thread.MAX_PRIORITY);
                return thread;
            }
        });

        midiExecutor.submit(this::setupMidi);
    }

    private void setupMidi() {
        // Found the primary keyboard
        final MidiDevice keyboard = MidiDeviceFinder.findExquis();

        final Synthesizer synth;
        try {
            synth = MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }

        final Soundbank soundbank;
        try {
            soundbank = MidiSystem.getSoundbank(
                    new File(System.getProperty("user.home"), ".gervill/soundbank-emg.sf2")
            );
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            keyboard.open();
            synth.open();

            synth.loadAllInstruments(soundbank);
            final MidiChannel[] channels = synth.getChannels();
            channels[0].programChange(12); // Example: Change to an instrument

            final Transmitter transmitter = keyboard.getTransmitter();
            final Receiver printerReceiver = new MidiInputReceiver();
            final Receiver synthReceiver = synth.getReceiver();

            // Create a forwarding receiver
            final Receiver forwardingReceiver = new Receiver() {
                @Override
                public void send(MidiMessage message, long timeStamp) {
                    if (message instanceof ShortMessage) {
                        final ShortMessage sm = (ShortMessage) message;
                        int key = sm.getData1();
                        int velocity = sm.getData2();

                        // Forward message to custom receiver
                        printerReceiver.send(message, timeStamp);

                        // To play, we override the message and force volume to 127
                        final ShortMessage overriddenMessage = new ShortMessage();
                        try {
                            overriddenMessage.setMessage(sm.getCommand(), sm.getChannel(), key, 127);
                        } catch (InvalidMidiDataException e) {
                            throw new RuntimeException(e);
                        }

                        // Forward message to the default MIDI synth
                        synthReceiver.send(overriddenMessage, -1); // -1 to process immediately

                        if (sm.getCommand() == ShortMessage.NOTE_ON) {
                            int midiNote = sm.getData1();
                            Platform.runLater(() -> fxmlController.playNote(midiNote));
                        }
                    }
                }

                @Override
                public void close() {
                    printerReceiver.close();
                    synthReceiver.close();
                }
            };

            // Get the transmitter and receiver
            transmitter.setReceiver(forwardingReceiver);

            System.out.println("Listening for MIDI input... Press keys on your MIDI keyboard.");
            // Keep the program running to listen for MIDI input
            Thread.sleep(Long.MAX_VALUE);

        } catch (MidiUnavailableException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Ensure the devices are closed properly
            try {
                keyboard.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                synth.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (midiExecutor != null) {
            midiExecutor.shutdownNow();
        }
    }
}
