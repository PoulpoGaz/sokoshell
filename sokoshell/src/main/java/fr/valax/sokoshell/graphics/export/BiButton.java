package fr.valax.sokoshell.graphics.export;

import fr.valax.sokoshell.graphics.ActionListener;
import fr.valax.sokoshell.graphics.Component;
import fr.valax.sokoshell.graphics.Key;
import fr.valax.sokoshell.graphics.Surface;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.util.function.Consumer;

public class BiButton extends Component {

    private static final AttributedStyle SELECTED =
            AttributedStyle.DEFAULT.background(AttributedStyle.CYAN);

    private final AttributedString left;
    private final AttributedString right;

    private boolean leftSelected;

    public BiButton(String left, String right) {
        this.left = new AttributedString(left);
        this.right = new AttributedString(right);
    }

    @Override
    protected void drawComponent(fr.valax.sokoshell.graphics.Graphics g) {
        int halfWidth = getWidth() / 2;
        int xLeft = (halfWidth - left.length()) / 2;
        int xRight = halfWidth + (halfWidth - right.length()) / 2;

        Surface s = g.getSurface();
        if (leftSelected) {
            s.draw(left, SELECTED, xLeft, 0);
            s.draw(right, AttributedStyle.DEFAULT, xRight, 0);
        } else {
            s.draw(left, AttributedStyle.DEFAULT, xLeft, 0);
            s.draw(right, SELECTED, xRight, 0);
        }
    }

    @Override
    protected void updateComponent() {
        if (keyPressed(Key.ENTER)) {
            String command;
            if (leftSelected) {
                command = "left";
            } else {
                command = "right";
            }

            for (ActionListener l : getActionListeners()) {
                l.actionPerformed(this, command);
            }
        } else if (keyPressed(Key.LEFT) || keyPressed(Key.RIGHT)) {
            leftSelected = !leftSelected;
            repaint();
        }
    }

    @Override
    protected Dimension compPreferredSize() {
        Insets insets = getInsets();
        Dimension dim = new Dimension();
        dim.width = insets.right + insets.left + left.length() + 1 + right.length();
        dim.height = insets.top + insets.bottom + 1;

        return dim;
    }

    public void setLeftSelected(boolean leftSelected) {
        if (leftSelected != this.leftSelected) {
            this.leftSelected = leftSelected;
            repaint();
        }
    }

    public boolean isLeftSelected() {
        return leftSelected;
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(ActionListener.class, listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.add(ActionListener.class, listener);
    }

    public ActionListener[] getActionListeners() {
        return listeners.getListeners(ActionListener.class);
    }
}
