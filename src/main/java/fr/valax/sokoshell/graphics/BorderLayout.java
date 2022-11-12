package fr.valax.sokoshell.graphics;

import java.awt.*;

/**
 * @see java.awt.BorderLayout
 */
public class BorderLayout implements Layout {

    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int EAST = 2;
    public static final int WEST = 3;
    public static final int CENTER = 4;

    private Component north;
    private Component south;
    private Component east;
    private Component west;
    private Component center;

    private int verticalGap;
    private int horizontalGap;

    public BorderLayout() {

    }

    @Override
    public void addComponent(Component component, Object constraints) {
        if (constraints instanceof Integer c) {

            if (c == NORTH) {
                north = component;
            } else if (c == SOUTH) {
                south = component;
            } else if (c == EAST) {
                east = component;
            } else if (c == WEST) {
                west = component;
            } else if (c == CENTER) {
                center = component;
            } else {
                throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + component);
            }

        } else {
            throw new IllegalArgumentException("Cannot add to layout: constraint must be an integer");
        }
    }

    @Override
    public Dimension preferredSize(Component parent) {
        Dimension dim = new Dimension(0, 0);
        Component c;

        if ((c = east) != null) {
            Dimension d = c.getPreferredSize();
            dim.width += d.width + horizontalGap;
            dim.height = Math.max(d.height, dim.height);
        }
        if ((c = west) != null) {
            Dimension d = c.getPreferredSize();
            dim.width += d.width + horizontalGap;
            dim.height = Math.max(d.height, dim.height);
        }
        if ((c = center) != null) {
            Dimension d = c.getPreferredSize();
            dim.width += d.width;
            dim.height = Math.max(d.height, dim.height);
        }
        if ((c = north) != null) {
            Dimension d = c.getPreferredSize();
            dim.width = Math.max(d.width, dim.width);
            dim.height += d.height + verticalGap;
        }
        if ((c = south) != null) {
            Dimension d = c.getPreferredSize();
            dim.width = Math.max(d.width, dim.width);
            dim.height += d.height + horizontalGap;
        }

        Insets insets = parent.getInsets();
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;

        return dim;
    }

    @Override
    public void layout(Component parent) {
        Insets insets = parent.getInsets();
        int top = insets.top;
        int bottom = parent.getHeight() - insets.bottom;
        int left = insets.left;
        int right = parent.getWidth() - insets.right;

        Component c;
        if ((c = north) != null) {
            c.setSize(right - left, c.getHeight());
            Dimension d = c.getPreferredSize();
            c.setBounds(left, top, right - left, d.height);
            top += d.height + verticalGap;
        }
        if ((c = south) != null) {
            c.setSize(right - left, c.getHeight());
            Dimension d = c.getPreferredSize();
            c.setBounds(left, bottom - d.height, right - left, d.height);
            bottom -= d.height + verticalGap;
        }
        if ((c = east) != null) {
            c.setSize(c.getWidth(), bottom - top);
            Dimension d = c.getPreferredSize();
            c.setBounds(right - d.width, top, d.width, bottom - top);
            right -= d.width + horizontalGap;
        }
        if ((c = west) != null) {
            c.setSize(c.getWidth(), bottom - top);
            Dimension d = c.getPreferredSize();
            c.setBounds(left, top, d.width, bottom - top);
            left += d.width + horizontalGap;
        }
        if ((c = center) != null) {
            c.setBounds(left, top, right - left, bottom - top);
        }
    }

    public int getVerticalGap() {
        return verticalGap;
    }

    public void setVerticalGap(int verticalGap) {
        this.verticalGap = verticalGap;
    }

    public int getHorizontalGap() {
        return horizontalGap;
    }

    public void setHorizontalGap(int horizontalGap) {
        this.horizontalGap = horizontalGap;
    }
}
