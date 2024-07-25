package com.amyodov.midi.cfdemo;

import javax.sound.midi.*;

public class MidiInputReceiver implements Receiver {
    public void send(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage) {
            final ShortMessage sm = (ShortMessage) message;
            if (sm.getCommand() == ShortMessage.NOTE_ON) {
                int key = sm.getData1();
                int velocity = sm.getData2();
                try {
                    System.out.printf("Note ON: %d (%s), velocity: %d; channel %d; position %d\n",
                            key,
                            MidiUtils.midiNoteToString(key),
                            velocity,
                            sm.getChannel(),
                            MidiUtils.getCircleOfFifthsPosition(key)
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (sm.getCommand() == ShortMessage.NOTE_OFF) {
                int key = sm.getData1();
                System.out.printf("Note OFF: %s\n", key);
            } else if (sm.getCommand() == ShortMessage.CONTROL_CHANGE) {
                System.out.printf("CC: data %s/%s\n", sm.getData1(), sm.getData2());
                // MiniLab Faders:
                //  data1: {7, 1, 2, 11};
                //  data2: 0..127
            } else {
                System.out.printf("Command %d: data %s/%s\n", sm.getCommand(), sm.getData1(), sm.getData2());
            }
        }
    }

    public void close() {
        // Not used in this example
        System.out.println("MIDI input receiver closed");
    }
}
