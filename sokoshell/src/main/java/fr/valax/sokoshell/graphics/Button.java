package fr.valax.sokoshell.graphics;

import org.jline.terminal.MouseEvent;
import org.jline.utils.AttributedString;

import java.awt.*;
import java.util.Objects;

public class Button extends Component {

    private AttributedString text;
    private boolean pressed;

    public Button() {

    }

    @Override
    protected void drawComponent(Graphics g) {
        Insets i = getInsets();

        if (text != null) {
            g.getSurface().draw(text,
                    (getWidth() - text.columnLength()) / 2,
                    (getHeight() - 1) / 2);
        }
    }

    @Override
    public void updateComponent() {
        if (engine.hasMouseEvent()) {
            MouseEvent evt = engine.getLastMouseEvent();
            boolean inside = isInside(evt.getX(), evt.getY());

            if (inside) {
                if (evt.getType() == MouseEvent.Type.Pressed) {
                    if (!pressed) {
                        pressed = true;
                        repaint();
                    }
                } else if (evt.getType() == MouseEvent.Type.Released) {
                    pressed = false;
                    repaint();

                    for (ActionListener l : getActionListeners()) {
                        l.actionPerformed(this, "released");
                    }
                }

            } else if (evt.getType() == MouseEvent.Type.Released) {
                pressed = false;
                repaint();
            }
        }
    }

    @Override
    protected Dimension compPreferredSize() {
        return GraphicsUtils.preferredSize(getInsets(), text);
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

    public boolean isPressed() {
        return pressed;
    }

    public AttributedString getText() {
        return text;
    }

    public void setText(AttributedString text) {
        if (!Objects.equals(this.text, text)) {
            this.text = text;
            repaint();
        }
    }
}
