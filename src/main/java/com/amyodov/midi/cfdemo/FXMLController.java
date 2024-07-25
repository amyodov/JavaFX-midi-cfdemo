package com.amyodov.midi.cfdemo;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class FXMLController implements Initializable {
    @FXML
    private Pane pane;

    private BigPixelGrid bigPixelGrid;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bigPixelGrid = new BigPixelGrid(pane);
    }


    public void playNote(int midiNote) {
        try {
            // Emit circle in the direction of the Circle of Fifths
            bigPixelGrid.emitCircle(midiNote);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
