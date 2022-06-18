package fr.valax.graph;

import java.awt.*;

public interface ScatterPlotPoint {

    void draw(Graphics2D g2d, int cx, int cy);

    default int size() {
        return 0;
    }

    class Circle implements ScatterPlotPoint {

        private boolean filled;
        private int radius;

        public Circle() {
            filled = true;
            radius = 4;
        }

        public Circle(int radius) {
            filled = true;
            this.radius = radius;
        }

        public Circle(boolean filled, int radius) {
            this.filled = filled;
            this.radius = radius;
        }

        @Override
        public void draw(Graphics2D g2d, int cx, int cy) {
            if (filled) {
                g2d.fillOval(cx - radius / 2, cy - radius / 2, radius, radius);
            } else {
                g2d.drawOval(cx - radius / 2, cy - radius / 2, radius, radius);
            }
        }

        @Override
        public int size() {
            return radius * 2;
        }

        public boolean isFilled() {
            return filled;
        }

        public void setFilled(boolean filled) {
            this.filled = filled;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }
    }

    class Rectangle implements ScatterPlotPoint {

        private boolean filled;
        private int size;

        public Rectangle() {
            filled = true;
            size = 4;
        }

        public Rectangle(boolean filled, int size) {
            this.filled = filled;
            this.size = size;
        }

        @Override
        public void draw(Graphics2D g2d, int cx, int cy) {
            if (filled) {
                g2d.fillRect(cx - size / 2, cy - size / 2, size, size);
            } else {
                g2d.drawRect(cx - size / 2, cy - size / 2, size, size);
            }
        }

        @Override
        public int size() {
            return size;
        }
    }
}