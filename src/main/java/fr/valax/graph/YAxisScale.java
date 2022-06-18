package fr.valax.graph;

import java.awt.*;
import java.awt.geom.AffineTransform;

import static fr.valax.graph.Utils.roundDown;
import static fr.valax.graph.Utils.roundUp;

public class YAxisScale extends Scale {

    public YAxisScale() {
    }

    public YAxisScale(int lengthInReality, int lengthInGraph) {
        super(lengthInReality, lengthInGraph);
    }

    @Override
    public Dimension preferredSize(int min, int max, int plotHeight) {
        labelCache.clear();

        Dimension dim = new Dimension();
        dim.height = plotHeight;

        int roundMin = roundUp(min, lengthInReality);
        int roundMax = roundDown(max, lengthInReality);

        int w = 0;
        for (int val = roundMin; val <= roundMax; val += lengthInReality) {
            Label label = toString(val);
            Dimension pref = label.preferredSize();

            w = Math.max(pref.width, w);

            if (val + lengthInGraph > roundMax) { // last label may be out of bounds
                int delta = getInGraph(max - val);

                int halfHeight = pref.height / 2;

                if (delta < halfHeight) { // label out of bounds
                    //insets.top = halfHeight - delta;
                    dim.height += (halfHeight - delta);
                }
            }
        }

        if (arrow != null) {
            dim.height += arrow.height;
        }

        dim.width = lineWidth + graduationSize + spaceBetweenText + w;

        if (title != null) {
            Dimension pref = title.preferredSize();
            dim.width += spaceBetweenText + pref.height; // title is rotated
        }

        return dim;
    }

    @Override
    protected void drawAxis(Graphics2D g2d, Dimension size, int min, int max, int plotHeight) {
        g2d.setColor(color);

        int y = 0;
        int height = size.height;
        if (arrow != null) {
            y = arrow.getWidth();
            height = size.height - arrow.getWidth();
        }

        g2d.fillRect(size.width - lineWidth, y, lineWidth, height);

        int minGraph = getInGraph(min);

        int roundMin = roundUp(min, lengthInReality);
        int roundMax = roundDown(max, lengthInReality);

        int graduationX = size.width - lineWidth - graduationSize;;

        for (int val = roundMin; val <= roundMax; val += lengthInReality) {
            int valGraph = getInGraph(val);

            y = size.height - (valGraph - minGraph);

            g2d.fillRect(graduationX, y - lineWidth / 2, graduationSize, lineWidth);

            Label label = toString(val);
            Dimension pref = label.preferredSize();

            int tx = size.width - lineWidth - graduationSize - spaceBetweenText - pref.width;
            int ty = y - pref.height / 2;

            g2d.translate(tx, ty);
            label.draw(g2d, pref);
            g2d.translate(-tx, -ty);
        }

        if (title != null) {
            Dimension pref = title.preferredSize();

            int ty = (size.height + pref.width) / 2;

            AffineTransform old = g2d.getTransform();

            g2d.translate(0, ty);
            g2d.rotate(-Math.PI / 2);
            title.draw(g2d, pref);

            g2d.setTransform(old);
        }

        if (arrow != null) {
            double tx = size.width - (arrow.getWidth() + lineWidth) / 2d;
            double ty = 0;

            g2d.translate(tx, ty);
            arrow.drawYAxis(g2d, color, lineWidth);
            g2d.translate(-tx, -ty);
        }
    }
}