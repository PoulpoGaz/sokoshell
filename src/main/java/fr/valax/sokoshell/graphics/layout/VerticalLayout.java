package fr.valax.sokoshell.graphics.layout;

import fr.valax.sokoshell.graphics.Component;

import java.awt.*;
import java.util.LinkedHashMap;

public class VerticalLayout implements Layout {

    public enum Orientation {
        TOP,
        BOTTOM
    }

    private final LinkedHashMap<Component, VerticalConstraint> constraints;

    private int defaultTopGap;
    private int defaultBottomGap;

    private int startGap;
    private int endGap;

    public VerticalLayout() {
        this(0, 0);
    }

    public VerticalLayout(int defaultGap) {
        defaultTopGap = Math.floorDiv(defaultGap, 2);
        defaultBottomGap = (int) Math.ceil(defaultGap / 2d);

        constraints = new LinkedHashMap<>();
    }

    public VerticalLayout(int defaultTopGap, int defaultBottomGap) {
        this.defaultTopGap = defaultTopGap;
        this.defaultBottomGap = defaultBottomGap;

        constraints = new LinkedHashMap<>();
    }

    public VerticalLayout(int defaultGap, int startGap, int endGap) {
        defaultTopGap = Math.floorDiv(defaultGap, 2);
        defaultBottomGap = (int) Math.ceil(defaultGap / 2d);
        this.startGap = startGap;
        this.endGap = endGap;

        constraints = new LinkedHashMap<>();
    }

    public VerticalLayout(int defaultTopGap, int defaultBottomGap, int startGap, int endGap) {
        this.defaultTopGap = defaultTopGap;
        this.defaultBottomGap = defaultBottomGap;
        this.startGap = startGap;
        this.endGap = endGap;

        constraints = new LinkedHashMap<>();
    }

    @Override
    public void addComponent(Component comp, Object c) {
        VerticalConstraint constraint;

        if (c == null) {
            constraint = new VerticalConstraint();
        } else if (!(c instanceof VerticalConstraint)) {
            throw new IllegalArgumentException("Cannot add " + c + " to layout. This isn't a VerticalConstraint");
        } else {
            constraint = (VerticalConstraint) ((VerticalConstraint) c).clone();
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
                continue;
            }

            VerticalConstraint constraint = getConstraintFor(component);

            Dimension componentDim = component.getPreferredSize();

            dim.width = Math.max(dim.width, componentDim.width);
            dim.height += componentDim.height + getTopGap(constraint) + getBottomGap(constraint);
        }

        Insets insets = parent.getInsets();

        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom + startGap + endGap;

        return dim;
    }

    @Override
    public void layout(Component parent) {
        Dimension dim = parent.getSize();
        Insets insets = parent.getInsets();

        int width = dim.width - insets.left - insets.right;

        int topY = insets.top + startGap;
        int botY = dim.height - insets.bottom - endGap;

        Component topEnd = null;
        Component botEnd = null;

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component component = parent.getComponent(i);

            if (!component.isVisible()) {
                component.setBounds(0, 0, 0, 0);

                continue;
            }

            Dimension compDim = component.getPreferredSize();

            VerticalConstraint constraint = getConstraintFor(component);

            if (constraint.endComponent) {
                if (constraint.orientation == Orientation.TOP) {
                    topEnd = component;
                } else {
                    botEnd = component;
                }

                continue;
            }

            int x;
            int w;

            if (constraint.fillXAxis) {
                x = insets.left;
                w = width;
            } else {
                w = Math.min(width, compDim.width);
                x = getXAlignment(width, w, constraint.xAlignment, insets.left);
            }

            if (constraint.orientation == Orientation.TOP) {
                topY += getTopGap(constraint);

                component.setBounds(x, topY, w, compDim.height);

                topY += getBottomGap(constraint) + compDim.height;
            } else {
                botY = botY - getBottomGap(constraint) - compDim.height;

                component.setBounds(x, botY, w, compDim.height);

                botY -= getTopGap(constraint);
            }
        }

        if (topEnd != null && botEnd != null) {
            int height = (botY - topY) / 2;

            placeEndComponent(topEnd, topY, topY + height, width, insets.left);
            placeEndComponent(botEnd, botY - height, botY, width, insets.left);

        } else if (topEnd != null) {
            placeEndComponent(topEnd, topY, botY, width, insets.left);
        } else if (botEnd != null) {
            placeEndComponent(botEnd, topY, botY, width, insets.left);
        }
    }

    private int getXAlignment(int parentWidth, int compWidth, float xAlignment, int leftX) {
        if(parentWidth == compWidth || parentWidth == 0) {
            return leftX;
        } else {
            return (int) (xAlignment * (parentWidth - compWidth) + leftX);
        }
    }

    private void placeEndComponent(Component component, int topY, int botY, int parentWidth, int leftX) {
        VerticalConstraint constraint = getConstraintFor(component);

        topY += getTopGap(constraint);
        int height = botY - topY - getBottomGap(constraint);

        int x;
        int w;
        if (constraint.fillXAxis) {
            x = leftX;
            w = parentWidth;
        } else {
            w = Math.min(component.getPreferredSize().width, parentWidth);
            x = getXAlignment(parentWidth, w, constraint.xAlignment, leftX);
        }

        component.setBounds(x, topY, w, height);
    }

    private int getTopGap(VerticalConstraint constraint) {
        if (constraint.topGap < 0) {
            return defaultTopGap;
        } else {
            return constraint.topGap;
        }
    }

    private int getBottomGap(VerticalConstraint constraint) {
        if (constraint.bottomGap < 0) {
            return defaultBottomGap;
        } else {
            return constraint.bottomGap;
        }
    }

    private VerticalConstraint getConstraintFor(Component component) {
        VerticalConstraint constraint = constraints.get(component);

        if (constraint == null) {
            constraint = new VerticalConstraint();

            constraints.put(component, constraint);
        }

        return constraint;
    }


    public int getDefaultTopGap() {
        return defaultTopGap;
    }

    public void setDefaultTopGap(int defaultTopGap) {
        this.defaultTopGap = Math.max(0, defaultTopGap);
    }

    public int getDefaultBottomGap() {
        return defaultBottomGap;
    }

    public void setDefaultBottomGap(int defaultBottomGap) {
        this.defaultBottomGap = Math.max(0, defaultBottomGap);
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