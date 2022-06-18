package fr.valax.graph;

import java.awt.*;

public interface ScatterPlotRenderer {

    void draw(Graphics2D g2d, ScatterPlotPoint point, Series series, int index, int x1, int y1, int x2, int y2);

    class Point implements ScatterPlotRenderer {

        @Override
        public void draw(Graphics2D g2d, ScatterPlotPoint point, Series series, int index, int x1, int y1, int x2, int y2) {
            point.draw(g2d, x2, y2);
        }
    }

    class Line implements ScatterPlotRenderer {

        @Override
        public void draw(Graphics2D g2d, ScatterPlotPoint point, Series series, int index, int x1, int y1, int x2, int y2) {
            if (index > 0) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    class LineWithPoint implements ScatterPlotRenderer {

        private int lineWidth;
        private BasicStroke stroke;

        public LineWithPoint(int lineWidth) {
            this.lineWidth = lineWidth;
        }

        @Override
        public void draw(Graphics2D g2d, ScatterPlotPoint point, Series series, int index, int x1, int y1, int x2, int y2) {
            point.draw(g2d, x2, y2);

            if (index > 0) {
                Stroke old = g2d.getStroke();

                if (stroke == null) {
                    stroke = new BasicStroke(lineWidth);
                }

                g2d.setStroke(stroke);
                g2d.drawLine(x1, y1, x2, y2);
                g2d.setStroke(old);
            }
        }

        public int getLineWidth() {
            return lineWidth;
        }

        public void setLineWidth(int lineWidth) {
            if (lineWidth > 0 && this.lineWidth != lineWidth) {
                this.lineWidth = lineWidth;
                stroke = null;
            }
        }
    }
}
