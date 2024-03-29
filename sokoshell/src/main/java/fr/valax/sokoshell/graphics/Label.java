package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedString;

import java.awt.*;
import java.util.Objects;

public class Label extends Component {

    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int CENTER = 2;
    public static final int EAST = 3;
    public static final int WEST = 4;

    private AttributedString text;
    private int horizAlign = CENTER;
    private int vertAlign = CENTER;

    public Label() {

    }

    public Label(AttributedString text) {
        setText(text);
    }

    public Label(String text) {
        setText(text);
    }

    public Label(AttributedString text, int horizAlign) {
        setText(text);
        setHorizAlign(horizAlign);
    }

    public Label(String text, int horizAlign) {
        setText(text);
        setHorizAlign(horizAlign);
    }

    public Label(AttributedString text, int horizAlign, int vertAlign) {
        setText(text);
        setHorizAlign(horizAlign);
        setVertAlign(vertAlign);
    }

    @Override
    public void drawComponent(Graphics g) {
        if (text == null) {
            return;
        }

        int length = text.columnLength();
        if (length == 0) {
            return;
        }

        int x;
        int y;
        if (horizAlign == CENTER) {
            x = (getWidth() - length) / 2;
        } else if (horizAlign == WEST) {
            x = 0;
        } else { // east
            x = getWidth() - length;
        }

        if (vertAlign == CENTER) {
            y = (getHeight() - 1) / 2;
        } else if (vertAlign == NORTH) {
            y = 0;
        } else { // south
            y = getHeight() - length;
        }

        g.getSurface().draw(text, x, y);
    }

    @Override
    protected Dimension compPreferredSize() {
        return GraphicsUtils.preferredSize(getInsets(), text);
    }

    public void setText(AttributedString text) {
        if (!Objects.equals(text, this.text)) {
            this.text = text;
            repaint();
        }
    }

    public void setText(String text) {
        setText(new AttributedString(text));
    }

    public AttributedString getText() {
        return text;
    }

    public int getHorizAlign() {
        return horizAlign;
    }

    public void setHorizAlign(int horizAlign) {
        if (horizAlign != CENTER && horizAlign != WEST && horizAlign != EAST) {
            throw new IllegalArgumentException("Horizontal alignment should be one of Label.CENTER, Label.WEST, Label.EAST");
        }

        if (horizAlign != this.horizAlign) {
            this.horizAlign = horizAlign;
            repaint();
        }
    }

    public int getVertAlign() {
        return vertAlign;
    }

    public void setVertAlign(int vertAlign) {
        if (vertAlign != CENTER && vertAlign != NORTH && vertAlign != SOUTH) {
            throw new IllegalArgumentException("Vertical alignment should be one of Label.CENTER, Label.NORTH, Label.SOUTH");
        }

        if (vertAlign != this.vertAlign) {
            this.vertAlign = vertAlign;
            repaint();
        }
    }
}
