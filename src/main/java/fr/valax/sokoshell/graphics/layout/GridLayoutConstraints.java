package fr.valax.sokoshell.graphics.layout;

public class GridLayoutConstraints implements Cloneable {

    public static final int NONE = 0;
    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;
    public static final int BOTH = 3;

    public int x;
    public int y;
    public int fill;

    public double xAlignment = 0.5;
    public double yAlignment = 0.5;

    public double weightX = 0;
    public double weightY = 0;

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
