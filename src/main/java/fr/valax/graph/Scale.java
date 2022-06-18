package fr.valax.graph;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static fr.valax.graph.Utils.roundDown;
import static fr.valax.graph.Utils.roundUp;

public abstract class Scale {

    protected static final Function<Integer, Label> TO_STRING = integer -> new Label(Integer.toString(integer));

    protected final Map<Integer, Label> labelCache = new HashMap<>();

    protected int lengthInReality = 1;
    protected int lengthInGraph = 1;

    protected Label title;
    protected Color color = Utils.TEXT_COLOR;
    protected Function<Integer, Label> nameFunction = TO_STRING;

    protected int lineWidth = 4;
    protected int spaceBetweenText = 5;
    protected int graduationSize = 10;

    protected Arrow arrow = new BasicArrow(20, 20);

    public Scale() {

    }

    public Scale(int lengthInReality, int lengthInGraph) {
        this.lengthInReality = lengthInReality;
        this.lengthInGraph = lengthInGraph;

        if (lengthInReality <= 0 || lengthInGraph <= 0) {
            throw new IllegalStateException();
        }
    }

    public abstract Dimension preferredSize(int min, int max, int plotSize);

    protected abstract void drawAxis(Graphics2D g2d, Dimension size, int min, int max, int plotSize);

    public Label toString(Integer integer) {
        Label label = labelCache.get(integer);

        if (label == null) {
            label = nameFunction.apply(integer);

            labelCache.put(integer, label);
        }

        return label;
    }

    public int getInGraph(int v) {
        return (int) ((double) v * lengthInGraph / lengthInReality);
    }

    public GraduationIterator graduationIterator(int min, int max) {
        return new GraduationIterator(min, max);
    }

    public void set(int lengthInReality, int lengthInGraph) {
        this.lengthInReality = lengthInReality;
        this.lengthInGraph = lengthInGraph;
    }

    public int getLengthInReality() {
        return lengthInReality;
    }

    public void setLengthInReality(int lengthInReality) {
        if (lengthInReality > 0) {
            this.lengthInReality = lengthInReality;
        }
    }

    public int getLengthInGraph() {
        return lengthInGraph;
    }

    public void setLengthInGraph(int lengthInGraph) {
        if (lengthInGraph > 0) {
            this.lengthInGraph = lengthInGraph;
        }
    }

    public int getGraduationSize() {
        return graduationSize;
    }

    public void setGraduationSize(int graduationSize) {
        this.graduationSize = graduationSize;
    }

    public Label getTitle() {
        return title;
    }

    public void setTitle(Label title) {
        this.title = title;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color != null) {
            this.color = color;
        }
    }

    public Function<Integer, Label> getNameFunction() {
        return nameFunction;
    }

    public void setNameFunction(Function<Integer, Label> nameFunction) {
        if (nameFunction != null && nameFunction != this.nameFunction) {
            this.nameFunction = nameFunction;

            labelCache.clear();
        }
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        if (lineWidth > 0) {
            this.lineWidth = lineWidth;
        }
    }

    public int getSpaceBetweenText() {
        return spaceBetweenText;
    }

    public void setSpaceBetweenText(int spaceBetweenText) {
        if (spaceBetweenText >= 0) {
            this.spaceBetweenText = spaceBetweenText;
        }
    }

    public Arrow getArrow() {
        return arrow;
    }

    public void setArrow(Arrow arrow) {
        this.arrow = arrow;
    }

    public class GraduationIterator implements Iterator<Integer> {

        private final int min;
        private final int max;

        private int val;

        public GraduationIterator(int min, int max) {
            this.min = roundUp(min, lengthInReality);
            this.max = roundDown(max, lengthInReality);

            val = this.min;
        }

        @Override
        public boolean hasNext() {
            return val <= max;
        }

        @Override
        public Integer next() {
            int v = val;
            val += lengthInReality;
            return v;
        }

        public void reset() {
            val = min;
        }
    }
}