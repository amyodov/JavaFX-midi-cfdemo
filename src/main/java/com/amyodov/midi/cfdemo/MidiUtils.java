package com.amyodov.midi.cfdemo;

public class MidiUtils {

    // Circle of Fifths positions in order
    private static final String[] CIRCLE_OF_FIFTHS = {
            "C", "G", "D", "A", "E", "B",
            "F♯/G♭", "C♯/D♭", "G♯/A♭", "D♯/E♭", "A♯/B♭", "E♯/F"
    };

    // For each midi note (normalized to range 0..11), its position on circle of fifths
    private static final int[] CIRCLE_OF_FIFTHS_POSITIONS = {
            0, //  0: C
            7, //  1: C♯
            2, //  2: D
            9, //  3: D♯
            4, //  4: E
            11, //  5: F
            6, //  6: F♯
            1, //  7: G
            8, //  8: G♯
            3, //  9: A
            10, // 10: A♯
            5, // 11: B
    };

    public static int getCircleOfFifthsPosition(int midiNote) {
        // Normalize MIDI note to a range of 0-11
        final int normalizedNote = (midiNote % 12 + 12) % 12;
        // Map to Circle of Fifths position
        return CIRCLE_OF_FIFTHS_POSITIONS[normalizedNote];
    }

    // Converts MIDI note number to note name from the Circle of Fifths array
    public static String midiNoteToString(int midiNote) {
        return CIRCLE_OF_FIFTHS[getCircleOfFifthsPosition(midiNote)];
    }
}
