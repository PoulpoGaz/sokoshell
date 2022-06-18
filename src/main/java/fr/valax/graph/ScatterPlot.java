package fr.valax.graph;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ScatterPlot {

    private final Window window = new Window();

    private Color background;
    private Color gridColor;
    private int gridLineWidth;
    private Label title;

    private Scale xAxisScale;
    private Scale yAxisScale;

    private List<Series> series;

    private boolean showLegend;

    public ScatterPlot() {
        background = Utils.BACKGROUND_2;
        gridColor = Utils.BACKGROUND_1;
        gridLineWidth = 2;
        title = new Label("Scatter plot");

        xAxisScale = new XAxisScale(1, 50);
        yAxisScale = new YAxisScale(1, 50);

        series = new ArrayList<>();
    }

    public void adaptWindow() {
        if (series.size() == 0) {
            throw new IllegalStateException("Empty plot, cannot adapt window");
        }

        window.minX = getMin(p -> p.x);
        window.minY = getMin(p -> p.y);

        window.maxX = getMax(p -> p.x);
        window.maxY = getMax(p -> p.y);
    }


    public void adaptAtOrigin() {
        if (series.size() == 0) {
            throw new IllegalStateException("Empty plot, cannot adapt window");
        }

        window.minX = 0;
        window.minY = 0;

        window.maxX = getMax(p -> p.x);
        window.maxY = getMax(p -> p.y);
    }

    public BufferedImage createAtOrigin() {
        adaptAtOrigin();

        return create(window);
    }

    public BufferedImage create() {
        adaptWindow();

        return create(window);
    }

    public BufferedImage create(Window window) {
        if (window == null) {
            throw new IllegalStateException("Window is null");
        }
        if (!window.isValid()) {
            throw new IllegalStateException("Window isn't valid: " + window);
        }

        int plotWidth = window.getWidth(xAxisScale);
        int plotHeight = window.getHeight(yAxisScale);

        return create(window, plotWidth, plotHeight);
    }

    public BufferedImage create(int plotWidth, int plotHeight) {
        adaptWindow();

        return createAndAdaptScale(plotWidth, plotHeight);
    }

    public BufferedImage createAtOrigin(int plotWidth, int plotHeight) {
        adaptAtOrigin();

        return createAndAdaptScale(plotWidth, plotHeight);
    }

    protected BufferedImage createAndAdaptScale(int plotWidth, int plotHeight) {
        int space = 40;

        int nLineW = plotWidth / space;
        int nLineH = plotHeight / space;

        int width = window.maxX - window.minX;
        int height = window.maxY - window.minY;

        xAxisScale.set(Math.max(1, width / nLineW),
                space);

        yAxisScale.set(Math.max(1, height / nLineH),
                space);

        return create(window, plotWidth, plotHeight);
    }

    public BufferedImage create(Window window, int plotWidth, int plotHeight) {
        if (window == null) {
            throw new IllegalStateException("Window is null");
        }
        if (!window.isValid()) {
            throw new IllegalStateException("Window isn't valid: " + window);
        }

        Dimension titlePref = getTitleSize();
        Dimension legendSize = getLegendSize();

        int oldLengthInGraph = yAxisScale.getLengthInGraph();

        if (plotHeight < legendSize.height) {
            oldLengthInGraph = adaptScaleForLegend(yAxisScale, legendSize.height, plotHeight);

            plotHeight = legendSize.height;
        }

        Dimension xAxisSize = xAxisScale.preferredSize(window.minX, window.maxX, plotWidth);
        Dimension yAxisSize = yAxisScale.preferredSize(window.minY, window.maxY, plotHeight);

        int imageWidth = yAxisSize.width + xAxisSize.width + legendSize.width;
        imageWidth = Math.max(imageWidth, titlePref.width);

        int imageHeight = Math.max(xAxisSize.height + yAxisSize.height, legendSize.height);
        imageHeight += titlePref.height;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        draw(image,
                titlePref,
                xAxisSize,
                yAxisSize,
                legendSize,
                plotWidth,
                plotHeight);

        yAxisScale.setLengthInGraph(oldLengthInGraph);

        return image;
    }

    //  --- DIMENSIONS ---

    protected int getMax(Function<Point, Integer> function) {
        int max = 0;

        for (Series s : series) {
            for (Point point : s.getPoints()) {
                max = Math.max(function.apply(point), max);
            }
        }

        return max;
    }

    protected int getMin(Function<Point, Integer> function) {
        int min = 0;

        for (Series s : series) {
            for (Point point : s.getPoints()) {
                min = Math.min(function.apply(point), min);
            }
        }

        return min;
    }

    protected Dimension getTitleSize() {
        if (title == null) {
            return new Dimension();
        } else {
            return new Dimension(title.preferredSize());
        }
    }

    protected Dimension getLegendSize() {
        Dimension dim = new Dimension();

        if (showLegend) {
            int pointW = 0;
            int labelW = 0;
            for (Series s : series) {
                Label label = s.getNameAsLabel();

                Dimension pref = label.preferredSize();
                ScatterPlotPoint point = s.getPoint();

                pointW = Math.max(point.size(), pointW);
                labelW = Math.max(pref.width, labelW);

                dim.height += Math.max(pref.height, point.size());
            }

            dim.width = pointW + 5 + labelW;
            dim.height += (series.size() - 1) * 5;
        }

        return dim;
    }

    protected int adaptScaleForLegend(Scale scale, int preferred, int current) {
        if (current >= preferred) {
            return current;
        }

        /*
            length in graph           <=> current
            preferred length in graph <=> preferred
         */

        int old = scale.getLengthInGraph();

        double newVal = (double) scale.getLengthInGraph() * preferred / current;
        scale.setLengthInGraph((int) newVal);

        return old;
    }

    //    --- DRAW ---

    protected void draw(BufferedImage image,
                        Dimension titlePref,
                        Dimension xAxisSize,
                        Dimension yAxisSize,
                        Dimension legendSize,
                        int plotWidth,
                        int plotHeight) {
        Graphics2D g2d = image.createGraphics();
        try {
            Dimension plotSize = new Dimension(plotWidth, plotHeight);

            Utils.setup(g2d, image.getWidth(), image.getHeight(), null, background);

            if (title != null) {
                titlePref.width = image.getWidth();

                title.draw(g2d, titlePref);

                g2d.translate(0, titlePref.height);
            }

            if (gridColor != null) {
                int ty = yAxisSize.height - plotHeight;

                g2d.translate(yAxisSize.width, ty);
                drawGrid(g2d, plotSize, window);
                g2d.translate(-yAxisSize.width, -ty);
            }

            yAxisScale.drawAxis(g2d, yAxisSize, window.minY, window.maxY, plotHeight);

            g2d.translate(yAxisSize.width, yAxisSize.height);
            xAxisScale.drawAxis(g2d, xAxisSize, window.minX, window.maxX, plotWidth);

            g2d.translate(0, -plotHeight);
            drawPlot(g2d, plotSize);

            g2d.translate(xAxisSize.width, plotHeight - yAxisSize.height);
            drawLegend(g2d, legendSize);
        } finally {
            g2d.dispose();
        }
    }

    protected void drawPlot(Graphics2D g2d, Dimension size) {
        for (Series series : this.series) {
            series.draw(g2d, xAxisScale, yAxisScale, size);
        }
    }

    protected void drawGrid(Graphics2D g2d, Dimension size, Window window) {
        Stroke old = g2d.getStroke();

        g2d.setStroke(new BasicStroke(gridLineWidth));
        g2d.setColor(gridColor);

        // x
        Scale.GraduationIterator iterator = xAxisScale.graduationIterator(window.minX, window.maxX);

        int minGraphX = xAxisScale.getInGraph(window.minX);

        while (iterator.hasNext()) {
            int x = xAxisScale.getInGraph(iterator.next()) - minGraphX;

            g2d.drawLine(x, 0, x, size.height);
        }

        // y
        iterator = yAxisScale.graduationIterator(window.minY, window.maxY);
        int minGraphY = yAxisScale.getInGraph(window.minY);

        while (iterator.hasNext()) {
            int y = yAxisScale.getInGraph(iterator.next()) - minGraphY;

            y = size.height - y;

            g2d.drawLine(0, y, size.width, y);
        }

        g2d.setStroke(old);
    }

    protected void drawLegend(Graphics2D g2d, Dimension size) {
        int y = 0;

        for (Series series : this.series) {
            ScatterPlotPoint p = series.getPoint();

            Label label = series.getNameAsLabel();
            Dimension pref = label.preferredSize();

            int rowHeight = Math.max(p.size(), pref.height);

            g2d.setColor(series.getColor());
            p.draw(g2d, p.size() / 2, y + rowHeight / 2);

            int tx = p.size() + 5;
            int ty = y + (rowHeight - pref.height) / 2;

            g2d.translate(tx, ty);
            label.draw(g2d, pref);
            g2d.translate(-tx, -ty);

            y += rowHeight + 5;
        }
    }


    public void addSeries(Series series) {
        if (series != null) {
            this.series.add(series);
        }
    }

    public void removeAllSeries() {
        series.clear();
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        if (background != null) {
            this.background = background;
        }
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public int getGridLineWidth() {
        return gridLineWidth;
    }

    public void setGridLineWidth(int gridLineWidth) {
        if (gridLineWidth > 0) {
            this.gridLineWidth = gridLineWidth;
        }
    }

    public Label getTitle() {
        return title;
    }

    public void setTitle(Label title) {
        if (title != null) {
            this.title = title;
        }
    }

    public Scale getXAxisScale() {
        return xAxisScale;
    }

    public void setXAxisScale(Scale xAxisScale) {
        if (xAxisScale != null) {
            this.xAxisScale = xAxisScale;
        }
    }

    public Scale getYAxisScale() {
        return yAxisScale;
    }

    public void setYAxisScale(Scale yAxisScale) {
        if (yAxisScale != null) {
            this.yAxisScale = yAxisScale;
        }
    }

    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        if (series != null) {
            this.series = series;
        }
    }

    public boolean isShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    public Window getWindow() {
        return window;
    }
}