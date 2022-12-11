package fr.valax.sokoshell.graphics.layout;

public class VerticalConstraint implements Cloneable {

    public static final int DEFAULT_GAP = -1;

    public VerticalLayout.Orientation orientation;
    public int topGap;
    public int bottomGap;

    public boolean endComponent;

    public float xAlignment;
    public boolean fillXAxis;

    public VerticalConstraint() {
        this(VerticalLayout.Orientation.TOP);
    }

    public VerticalConstraint(VerticalLayout.Orientation orientation) {
        this(orientation, DEFAULT_GAP, DEFAULT_GAP, false, 0.5f, false);
    }

    public VerticalConstraint(VerticalLayout.Orientation orientation,
                              int topGap, int bottomGap,
                              boolean endComponent,
                              float xAlignment, boolean fillXAxis) {
        this.orientation = orientation;
        this.topGap = topGap;
        this.bottomGap = bottomGap;
        this.endComponent = endComponent;
        this.xAlignment = xAlignment;
        this.fillXAxis = fillXAxis;
    }

    @Override
    public String toString() {
        return "VerticalConstraint{" +
                "orientation=" + orientation +
                ", topGap=" + topGap +
                ", bottomGap=" + bottomGap +
                ", endComponent=" + endComponent +
                ", xAlignment=" + xAlignment +
                '}';
    }

    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }
}