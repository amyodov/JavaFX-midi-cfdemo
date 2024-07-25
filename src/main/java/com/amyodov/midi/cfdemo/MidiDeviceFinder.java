package com.amyodov.midi.cfdemo;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.util.Arrays;

public class MidiDeviceFinder {
    public static MidiDevice findMinilab() {
        final MidiDevice.Info deviceInfo = Arrays.stream(MidiSystem.getMidiDeviceInfo())
                .filter(info -> info.getName().equals("MIDI"))
                .filter(info -> info.getVendor().equals("Arturia"))
                .filter(info -> info.getDescription().equals("Arturia MiniLab3 MIDI"))
                .filter(info -> {
                    final MidiDevice device;
                    try {
                        device = MidiSystem.getMidiDevice(info);
                    } catch (MidiUnavailableException e) {
                        throw new RuntimeException(e);
                    }
                    return device.getMaxTransmitters() == -1;
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("MIDI keyboard not found"));

        try {
            return MidiSystem.getMidiDevice(deviceInfo);
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public static MidiDevice findExquis() {
        final MidiDevice.Info deviceInfo = Arrays.stream(MidiSystem.getMidiDeviceInfo())
                .filter(info -> info.getName().equals("Exquis"))
                .filter(info -> info.getVendor().equals("Intuitive Instruments"))
                .filter(info -> info.getDescription().equals("Exquis"))
                .filter(info -> {
                    final MidiDevice device;
                    try {
                        device = MidiSystem.getMidiDevice(info);
                    } catch (MidiUnavailableException e) {
                        throw new RuntimeException(e);
                    }
                    return device.getMaxTransmitters() == -1;
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("MIDI keyboard not found"));

        try {
            return MidiSystem.getMidiDevice(deviceInfo);
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public static MidiDevice findGervill() {
        final MidiDevice.Info deviceInfo = Arrays.stream(MidiSystem.getMidiDeviceInfo())
                .filter(info -> info.getName().equals("Gervill"))
                .filter(info -> info.getVendor().equals("OpenJDK"))
                .filter(info -> info.getDescription().equals("Software MIDI Synthesizer"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("MIDI keyboard not found"));

        try {
            return MidiSystem.getMidiDevice(deviceInfo);
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}
