package fr.valax.graph;

import java.awt.*;

public class BasicArrow extends Arrow {

    public BasicArrow(int width, int height) {
        super(width, height);
    }

    @Override
    public void drawXAxis(Graphics2D g2d, Color color, int lineWidth) {
        g2d.setColor(color);

        Stroke old = g2d.getStroke();

        double lineHalf = lineWidth / 2d;

        g2d.translate(lineHalf, lineHalf);

        int width = this.width - lineWidth;
        int height = this.height - lineWidth;

        int y = height / 2;

        g2d.setStroke(new BasicStroke(lineWidth));
        g2d.drawLine(0, y, width - lineWidth, y);

        g2d.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));

        int x = width - y; // x - height / 2
        g2d.drawLine(x, 0, width, y);

        g2d.drawLine(x, height, width, y);

        g2d.setStroke(old);
        g2d.translate(-lineHalf, -lineHalf);
    }

    @Override
    public void drawYAxis(Graphics2D g2d, Color color, int lineWidth) {
        g2d.setColor(color);

        Stroke old = g2d.getStroke();

        double lineHalf = lineWidth / 2d;
        g2d.translate(lineHalf, lineHalf);

        int width = this.width - lineWidth;
        int height = this.height - lineWidth;

        int x = width / 2;

        g2d.setStroke(new BasicStroke(lineWidth));
        g2d.drawLine(x, lineWidth, x, height);

        g2d.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));

        int y = width / 2; // y - width / 2
        g2d.drawLine(0, y, x, 0);

        g2d.drawLine(x, 0, width, y);

        g2d.setStroke(old);
        g2d.translate(-lineHalf, -lineHalf);
    }
}