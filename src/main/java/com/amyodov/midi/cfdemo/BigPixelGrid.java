package com.amyodov.midi.cfdemo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class BigPixelGrid {
    public static final double BIGPIXEL_SIZE = 64.0; // Size of each bigpixel
    private static final double NOTE_SPEED = 10.0; // Speed of the ball, in bigpixel diameters
    private static final double NOTE_DIAMETER = 4.0; // Radius of the ball, in bigpixel diameters

    private static final double DEFAULT_CIRCLE_SIZE = 8.0; // Default size when unlit

    private static final double MIN_BIGPIXEL_SIZE = BIGPIXEL_SIZE / 6;
    private static final double MAX_BIGPIXEL_SIZE = BIGPIXEL_SIZE / 2;

    private static final Duration ANIMATION_DURATION = Duration.seconds(2);

    private static final int MAX_COLOR_COMPONENT = 191;
    private static final int MIN_COLOR_COMPONENT = 63;

    private static final double SIZE_PHASE_SHIFT_X = -0.3;
    private static final double SIZE_PHASE_SHIFT_Y = 0.1;

    private static final double COLOR_PHASE_SHIFT_X = 0.1;
    private static final double COLOR_PHASE_SHIFT_Y = 0.3;

    private Pane pane;
    private Circle[][] circleGrid;

    public BigPixelGrid(Pane pane) {
        this.pane = pane;
        recreateGrid();
        addPaneResizeListener();
    }

    private void recreateGrid() {
        pane.getChildren().clear();

        final int height = (int) (pane.getHeight() / BIGPIXEL_SIZE);
        final int width = (int) (pane.getWidth() / BIGPIXEL_SIZE);

        circleGrid = new Circle[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                final Circle circle = new Circle(DEFAULT_CIRCLE_SIZE / 2);
                circle.setFill(Color.GRAY);
                pane.getChildren().add(circle);
                circleGrid[i][j] = circle;
                circle.setCenterX(j * BIGPIXEL_SIZE + BIGPIXEL_SIZE / 2);
                circle.setCenterY(i * BIGPIXEL_SIZE + BIGPIXEL_SIZE / 2);
            }
        }

        final Timeline breathTimeline = new Timeline(new KeyFrame(Duration.millis(30), event -> updateCirclesBreathing()));
        breathTimeline.setCycleCount(Timeline.INDEFINITE);
        breathTimeline.play();
    }

    private void addPaneResizeListener() {
        pane.widthProperty().addListener((obs, oldVal, newVal) -> recreateGrid());
        pane.heightProperty().addListener((obs, oldVal, newVal) -> recreateGrid());
    }

    private void updateCirclesBreathing() {
        final double time = System.currentTimeMillis() % ANIMATION_DURATION.toMillis();
        final double progress = time / ANIMATION_DURATION.toMillis();

        for (int i = 0; i < circleGrid.length; i++) {
            for (int j = 0; j < circleGrid[i].length; j++) {
                final Circle circle = circleGrid[i][j];

                // Calculate phase shift to stagger the animation
                final double sizePhaseShiftX = i * SIZE_PHASE_SHIFT_X;
                final double sizePhaseShiftY = j * SIZE_PHASE_SHIFT_Y;
                final double sizePhase = (progress + sizePhaseShiftX + sizePhaseShiftY) % 1.0;

                // Apply easing function for smoother animation
                final double easedSizePhase = 0.5 - 0.5 * Math.cos(2 * Math.PI * sizePhase);
                final double size = MIN_BIGPIXEL_SIZE + (MAX_BIGPIXEL_SIZE - MIN_BIGPIXEL_SIZE) * easedSizePhase;

                circle.setRadius(size / 2.0);

                // Color breathing

                // Calculate phase shift for color
                final double colorPhaseShiftX = i * COLOR_PHASE_SHIFT_X;
                final double colorPhaseShiftY = j * COLOR_PHASE_SHIFT_Y;

                // Calculate color components
                final int red = (int) (MIN_COLOR_COMPONENT + (MAX_COLOR_COMPONENT - MIN_COLOR_COMPONENT) * Math.sin(2 * Math.PI * (progress + colorPhaseShiftX)));
                final int green = (int) (MIN_COLOR_COMPONENT + (MAX_COLOR_COMPONENT - MIN_COLOR_COMPONENT) * Math.sin(2 * Math.PI * (progress + colorPhaseShiftY)));
                final int blue = (int) (MIN_COLOR_COMPONENT + (MAX_COLOR_COMPONENT - MIN_COLOR_COMPONENT) * Math.sin(2 * Math.PI * (progress + colorPhaseShiftX + colorPhaseShiftY)));

                // Update circle color
                circle.setFill(Color.rgb(
                        // Ensure color components are within range
                        Math.max(MIN_COLOR_COMPONENT, Math.min(MAX_COLOR_COMPONENT, red)),
                        Math.max(MIN_COLOR_COMPONENT, Math.min(MAX_COLOR_COMPONENT, green)),
                        Math.max(MIN_COLOR_COMPONENT, Math.min(MAX_COLOR_COMPONENT, blue))
                ));
            }
        }
    }

    public void emitCircle(int midiNote) {
        final int position = MidiUtils.getCircleOfFifthsPosition(midiNote);
        final double angle = Math.toRadians(-30 * position + 90);

        // Center coordinates
        final double centerX = pane.getWidth() / 2;
        final double centerY = pane.getHeight() / 2;

        final double dx = Math.cos(angle) * BIGPIXEL_SIZE;
        final double dy = -Math.sin(angle) * BIGPIXEL_SIZE;

        // Get the color of the middle circle
        final int middleX = circleGrid[0].length / 2;
        final int middleY = circleGrid.length / 2;
        final Circle middleCircle = circleGrid[middleY][middleX];
        final Color middleColor = (Color) middleCircle.getFill();

        final double lightenOffset = (255 - MAX_COLOR_COMPONENT) / 255.0;

        final Circle ball = new Circle(NOTE_DIAMETER * BIGPIXEL_SIZE / 2.0);
        // The note color will be the lighter version of middle pixel color
        ball.setFill(new Color(
                Math.min(middleColor.getRed() + lightenOffset, 1.0),
                Math.min(middleColor.getGreen() + lightenOffset, 1.0),
                Math.min(middleColor.getBlue() + lightenOffset, 1.0),
                0.75
        ));
        pane.getChildren().add(ball);
        ball.setCenterX(centerX);
        ball.setCenterY(centerY);
        final Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), event -> {
            double cx = ball.getCenterX();
            double cy = ball.getCenterY();
            cx += dx * NOTE_SPEED / 100;
            cy += dy * NOTE_SPEED / 100;
            ball.setCenterX(cx);
            ball.setCenterY(cy);

            if (cx < 0 || cx > pane.getWidth() || cy < 0 || cy > pane.getHeight()) {
                pane.getChildren().remove(ball);
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
