package fr.valax.graph;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Series {

    private ScatterPlotPoint point;
    private ScatterPlotRenderer render;

    private List<Point> points;
    private Color color = Utils.TEXT_COLOR;
    private String name;

    private Label label;

    public Series() {
        points = new ArrayList<>();

        point = new ScatterPlotPoint.Circle(12);
        render = new ScatterPlotRenderer.LineWithPoint(4);
    }

    public void draw(Graphics2D g2d, Scale xScale, Scale yScale, Dimension size) {
        g2d.setColor(color);

        int lastX = -1;
        int lastY = -1;
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            int x = xScale.getInGraph(point.x);

            int y = size.height - yScale.getInGraph(point.y);

            render.draw(g2d, this.point, this, i, lastX, lastY, x, y);

            lastX = x;
            lastY = y;
        }
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void clear() {
        points.clear();
    }

    public int nPoints() {
        return points.size();
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        if (points != null) {
            this.points = points;
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color != null) {
            this.color = color;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!Objects.equals(name, this.name)) {
            this.name = name;

            label = null;
        }
    }

    public Label getNameAsLabel() {
        if (label == null && name != null) {
            label = new Label(name);
        }

        return label;
    }

    public ScatterPlotPoint getPoint() {
        return point;
    }

    public void setPoint(ScatterPlotPoint point) {
        if (point != null) {
            this.point = point;
        }
    }

    public ScatterPlotRenderer getRender() {
        return render;
    }

    public void setRender(ScatterPlotRenderer render) {
        if (render != null) {
            this.render = render;
        }
    }
}