package fr.valax.sokoshell.graphics.layout;

import fr.valax.sokoshell.graphics.Component;

import java.awt.*;
import java.util.LinkedHashMap;

public class GridLayout implements Layout {

    private final LinkedHashMap<Component, GridLayoutConstraints> constraints = new LinkedHashMap<>();


    private int[] columnWidth;
    private int[] rowsHeight;


    @Override
    public void addComponent(Component component, Object c) {
        GridLayoutConstraints constraint;

        if (c == null) {
            constraint = new GridLayoutConstraints();
        } else if (!(c instanceof GridLayoutConstraints)) {
            throw new IllegalArgumentException("Cannot add " + c + " to layout. This isn't a GridLayoutConstraints");
        } else {
            constraint = (GridLayoutConstraints) ((GridLayoutConstraints) c).clone();
        }

        constraints.put(component, constraint);
    }

    @Override
    public void removeComponent(Component component) {
        constraints.remove(component);
    }

    private void prepareArray(Component parent) {
        int maxX = 0;
        int maxY = 0;

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component comp = parent.getComponent(i);
            if (!comp.isVisible()) {
                continue;
            }

            GridLayoutConstraints c = constraints.get(comp);
            maxX = Math.max(maxX, c.x + 1);
            maxY = Math.max(maxY, c.y + 1);
        }

        columnWidth = new int[maxX];
        rowsHeight = new int[maxY];

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component comp = parent.getComponent(i);
            if (!comp.isVisible()) {
                continue;
            }

            GridLayoutConstraints c = constraints.get(comp);

            Dimension pref = comp.getPreferredSize();
            columnWidth[c.x] = Math.max(columnWidth[c.x], pref.width);
            rowsHeight[c.y] = Math.max(rowsHeight[c.y], pref.height);
        }
    }

    @Override
    public Dimension preferredSize(Component parent) {
        prepareArray(parent);

        Insets i = parent.getInsets();
        Dimension dim = new Dimension();
        dim.width = i.left + i.right + sum(columnWidth);
        dim.height = i.top + i.bottom + sum(rowsHeight);

        columnWidth = null;
        rowsHeight = null;

        return dim;
    }

    @Override
    public void layout(Component parent) {
        prepareArray(parent);

        Insets insets = parent.getInsets();
        int width = parent.getWidth() - insets.left - insets.right;
        int height = parent.getHeight() - insets.top - insets.bottom;

        double[] weightX = new double[columnWidth.length];
        double[] weightY = new double[rowsHeight.length];

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component comp = parent.getComponent(i);
            if (!comp.isVisible()) {
                continue;
            }

            GridLayoutConstraints c = constraints.get(comp);

            weightX[c.x] = Math.max(weightX[c.x], c.weightX);
            weightY[c.y] = Math.max(weightY[c.y], c.weightY);
        }

        double weightXSum = sum(weightX);
        double weightYSum = sum(weightY);

        int minWidth = sum(columnWidth);
        int minHeight = sum(rowsHeight);

        int availableWidth = Math.max(width - minWidth, 0);
        int availableHeight = Math.max(height - minHeight, 0);

        // distribute extra space on horizontal axis
        if (availableWidth > 0) {
            for (int x = 0; x < weightX.length; x++) {
                if (weightX[x] > 0) {
                    columnWidth[x] += availableWidth * weightX[x] / weightXSum;
                }
            }
        }

        // distribute extra space on vertical axis
        if (availableHeight > 0) {
            for (int y = 0; y < weightY.length; y++) {
                if (weightY[y] > 0) {
                    rowsHeight[y] += availableHeight * weightY[y] / weightYSum;
                }
            }
        }

        // final width and height taken by the grid
        int w = sum(columnWidth);
        int h = sum(rowsHeight);

        int minX = insets.left + (Math.max(width - w, 0) / 2);
        int minY = insets.top + (Math.max(height - h, 0) / 2);

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component comp = parent.getComponent(i);
            if (!comp.isVisible()) {
                continue;
            }

            GridLayoutConstraints c = constraints.get(comp);

            int x = minX + sum(columnWidth, 0, c.x);
            int y = minY + sum(rowsHeight, 0, c.y);

            int maxW = columnWidth[c.x];
            int maxH = rowsHeight[c.y];

            if (c.fill == GridLayoutConstraints.BOTH) {
                comp.setBounds(x, y, maxW, maxH);
                continue;
            }

            Dimension dim = comp.getPreferredSize();

            int xOffset;
            int yOffset;
            int compWidth;
            int compHeight;
            if (c.fill == GridLayoutConstraints.HORIZONTAL) {
                compWidth = maxW;
                xOffset = 0;
            } else {
                compWidth = Math.min(maxW, dim.width);
                xOffset = (int) Math.round((maxW - compWidth) * c.xAlignment);
            }

            if (c.fill == GridLayoutConstraints.VERTICAL) {
                compHeight = maxH;
                yOffset = 0;
            } else {
                compHeight = Math.min(maxH, dim.height);
                yOffset = (int) Math.round((maxH - compHeight) * c.yAlignment);
            }

            comp.setBounds(x + xOffset, y + yOffset, compWidth, compHeight);
        }

        columnWidth = null;
        rowsHeight = null;
    }



    private double sum(double[] array) {
        return sum(array, 0, array.length);
    }

    private double sum(double[] array, int start, int end) {
        double sum = 0;

        for (int i = start; i < end; i++) {
            sum += array[i];
        }

        return sum;
    }

    private int sum(int[] array) {
        return sum(array, 0, array.length);
    }

    private int sum(int[] array, int start, int end) {
        int sum = 0;

        for (int i = start; i < end; i++) {
            sum += array[i];
        }

        return sum;
    }
}
