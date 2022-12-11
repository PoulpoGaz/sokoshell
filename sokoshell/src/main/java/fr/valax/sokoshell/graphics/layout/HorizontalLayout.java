package fr.valax.sokoshell.graphics.layout;

import fr.valax.sokoshell.graphics.Component;

import java.awt.*;
import java.util.LinkedHashMap;

public class HorizontalLayout implements Layout {

    public enum Orientation {
        LEFT,
        RIGHT
    }

    private final LinkedHashMap<Component, HorizontalConstraint> constraints;

    private int defaultLeftGap;
    private int defaultRightGap;

    private int startGap;
    private int endGap;

    public HorizontalLayout() {
        this(0, 0);
    }

    public HorizontalLayout(int defaultGap) {
        defaultLeftGap = Math.floorDiv(defaultGap, 2);
        defaultRightGap = (int) Math.ceil(defaultGap / 2d);

        constraints = new LinkedHashMap<>();
    }

    public HorizontalLayout(int defaultLeftGap, int defaultRightGap) {
        this.defaultLeftGap = defaultLeftGap;
        this.defaultRightGap = defaultRightGap;

        constraints = new LinkedHashMap<>();
    }

    public HorizontalLayout(int defaultGap, int startGap, int endGap) {
        defaultLeftGap = Math.floorDiv(defaultGap, 2);
        defaultRightGap = (int) Math.ceil(defaultGap / 2d);
        this.startGap = startGap;
        this.endGap = endGap;

        constraints = new LinkedHashMap<>();
    }

    public HorizontalLayout(int defaultLeftGap, int defaultRightGap, int startGap, int endGap) {
        this.defaultLeftGap = defaultLeftGap;
        this.defaultRightGap = defaultRightGap;
        this.startGap = startGap;
        this.endGap = endGap;

        constraints = new LinkedHashMap<>();
    }

    @Override
    public void addComponent(Component comp, Object c) {
        HorizontalConstraint constraint;

        if (c == null) {
            constraint = new HorizontalConstraint();
        } else if (!(c instanceof HorizontalConstraint)) {
            throw new IllegalArgumentException("Cannot add " + c + " to layout. This isn't a HorizontalConstraint");
        } else {
            constraint = (HorizontalConstraint) ((HorizontalConstraint) c).clone();
        }

        constraints.put(comp, constraint);
    }

    @Override
    public void removeComponent(Component component) {
        constraints.remove(component);
    }


    @Override
    public Dimension preferredSize(Component parent) {
        Dimension dim = new Dimension();

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component component = parent.getComponent(i);

            if (!component.isVisible()) {
                component.setBounds(0, 0, 0, 0);

                continue;
            }

            HorizontalConstraint constraint = getConstraintFor(component);

            Dimension componentDim = component.getPreferredSize();
            dim.width += componentDim.width + getLeftGap(constraint) + getRightGap(constraint);
            dim.height = Math.max(dim.height, componentDim.height);
        }

        Insets insets = parent.getInsets();

        dim.width += insets.left + insets.right + startGap + endGap;
        dim.height += insets.top + insets.bottom;

        return dim;
    }

    @Override
    public void layout(Component parent) {
        Dimension dim = parent.getSize();
        Insets insets = parent.getInsets();

        int height = dim.height - insets.top - insets.bottom;

        int leftX = insets.left + startGap;
        int rightX = dim.width - insets.right - endGap;

        Component leftEnd = null;
        Component rightEnd = null;

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component component = parent.getComponent(i);

            if (!component.isVisible()) {
                component.setBounds(0, 0, 0, 0);

                continue;
            }

            Dimension compDim = component.getPreferredSize();

            HorizontalConstraint constraint = getConstraintFor(component);

            if (constraint.endComponent) {
                if (constraint.orientation == Orientation.LEFT) {
                    leftEnd = component;
                } else {
                    rightEnd = component;
                }

                continue;
            }

            int h;
            int y;

            if (constraint.fillYAxis) {
                h = height;
                y = insets.top;
            } else {
                h = Math.min(compDim.height, height);
                y = getYAlignment(height, h, constraint.yAlignment, insets.top);
            }

            if (constraint.orientation == Orientation.LEFT) {
                leftX += getLeftGap(constraint);

                component.setBounds(leftX, y, compDim.width, h);

                leftX += getRightGap(constraint) + compDim.width;
            } else {
                rightX = rightX - getRightGap(constraint) - compDim.width;

                component.setBounds(rightX, y, compDim.width, h);

                rightX -= getLeftGap(constraint);
            }
        }

        if (leftEnd != null && rightEnd != null) {
            int width = (rightX - leftX) / 2;

            placeEndComponent(leftEnd, leftX, leftX + width, height, insets.top);
            placeEndComponent(rightEnd, rightX - width, rightX, height, insets.top);

        } else if (leftEnd != null) {
            placeEndComponent(leftEnd, leftX, rightX, height, insets.top);
        } else if (rightEnd != null) {
            placeEndComponent(rightEnd, leftX, rightX, height, insets.top);
        }
    }

    private int getYAlignment(int parentHeight, int compHeight, float yAlignment, int topY) {
        if(parentHeight == compHeight || parentHeight == 0) {
            return topY;
        } else {
            return (int) (yAlignment * (parentHeight - compHeight) + topY);
        }
    }

    private void placeEndComponent(Component component, int leftX, int rightX, int parentHeight, int topY) {
        HorizontalConstraint constraint = getConstraintFor(component);

        leftX += getLeftGap(constraint);
        int width = rightX - leftX - getRightGap(constraint);

        int h;
        int y;

        if (constraint.fillYAxis) {
            h = parentHeight;
            y = topY;
        } else {
            h = Math.min(component.getPreferredSize().height, parentHeight);
            y = getYAlignment(parentHeight, h, constraint.yAlignment, topY);
        }

        component.setBounds(leftX, y, width, h);
    }

    private int getLeftGap(HorizontalConstraint constraint) {
        if (constraint.leftGap < 0) {
            return defaultLeftGap;
        } else {
            return constraint.leftGap;
        }
    }

    private int getRightGap(HorizontalConstraint constraint) {
        if (constraint.rightGap < 0) {
            return defaultRightGap;
        } else {
            return constraint.rightGap;
        }
    }

    private HorizontalConstraint getConstraintFor(Component component) {
        HorizontalConstraint constraint = constraints.get(component);

        if (constraint == null) {
            constraint = new HorizontalConstraint();

            constraints.put(component, constraint);
        }

        return constraint;
    }

    public int getDefaultLeftGap() {
        return defaultLeftGap;
    }

    public void setDefaultLeftGap(int defaultLeftGap) {
        this.defaultLeftGap = Math.max(0, defaultLeftGap);
    }

    public int getDefaultRightGap() {
        return defaultRightGap;
    }

    public void setDefaultRightGap(int defaultRightGap) {
        this.defaultRightGap = Math.max(0, defaultRightGap);
    }

    public int getStartGap() {
        return startGap;
    }

    public void setStartGap(int startGap) {
        this.startGap = Math.max(0, startGap);
    }

    public int getEndGap() {
        return endGap;
    }

    public void setEndGap(int endGap) {
        this.endGap = Math.max(0, endGap);
    }
}