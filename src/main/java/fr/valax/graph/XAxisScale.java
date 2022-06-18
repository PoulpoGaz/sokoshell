package fr.valax.graph;

import java.awt.*;

import static fr.valax.graph.Utils.roundDown;
import static fr.valax.graph.Utils.roundUp;

public class XAxisScale extends Scale {

    public XAxisScale() {
    }

    public XAxisScale(int lengthInReality, int lengthInGraph) {
        super(lengthInReality, lengthInGraph);
    }

    @Override
    public Dimension preferredSize(int min, int max, int plotWidth) {
        labelCache.clear();

        Dimension dim = new Dimension();
        dim.width = plotWidth;

        int roundMin = roundUp(min, lengthInReality);
        int roundMax = roundDown(max, lengthInReality);

        int h = 0;
        for (int val = roundMin; val <= roundMax; val += lengthInReality) {
            Label label = toString(val);
            Dimension pref = label.preferredSize();

            h = Math.max(pref.height, h);

            if (val + lengthInGraph > roundMax) { // last label may be out of bounds
                int delta = getInGraph(max - val);

                int halfWidth = pref.width / 2;

                if (delta < halfWidth) { // label out of bounds
                    //insets.left = halfWidth - delta;
                    dim.width += (halfWidth - delta);
                }
            }
        }

        if (arrow != null) {
            dim.width += arrow.width;
        }

        dim.height = lineWidth + graduationSize + spaceBetweenText + h;

        if (title != null) {
            Dimension pref = title.preferredSize();
            dim.height += spaceBetweenText + pref.height;
        }

        return dim;
    }

    @Override
    protected void drawAxis(Graphics2D g2d, Dimension size, int min, int max, int plotWidth) {
        g2d.setColor(color);

        int width = size.width;
        if (arrow != null) {
            width = size.width - arrow.getWidth();
        }

        g2d.fillRect(0, 0, width, lineWidth);

        int minGraph = getInGraph(min);

        int roundMin = roundUp(min, lengthInReality);
        int roundMax = roundDown(max, lengthInReality);

        int maxY = 0;
        for (int val = roundMin; val <= roundMax; val += lengthInReality) {
            int valGraph = getInGraph(val);

            int x = valGraph - minGraph;

            g2d.setColor(color);
            g2d.fillRect(x - lineWidth / 2, lineWidth, lineWidth, graduationSize);

            Label label = toString(val);
            Dimension pref = label.preferredSize();

            double tx = x - pref.width / 2f;
            double ty = lineWidth + graduationSize + spaceBetweenText;

            g2d.translate(tx, ty);
            label.draw(g2d, pref);
            g2d.translate(-tx, -ty);

            maxY = Math.max((int) (ty + pref.height), maxY);
        }

        // draw title
        if (title != null) {
            Dimension pref = title.preferredSize();

            double tx = (size.width - pref.width) / 2f;
            double ty = maxY + spaceBetweenText;

            g2d.translate(tx, ty);
            title.draw(g2d, pref);
            g2d.translate(-tx, -ty);
        }

        if (arrow != null) {
            double tx = size.width - arrow.getWidth();
            double ty = (lineWidth - arrow.getHeight()) / 2d;

            g2d.translate(tx, ty);
            arrow.drawXAxis(g2d, color, lineWidth);
            g2d.translate(-tx, -ty);
        }
    }
}