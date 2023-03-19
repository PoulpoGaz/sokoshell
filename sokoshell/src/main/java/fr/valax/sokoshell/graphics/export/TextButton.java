package fr.valax.sokoshell.graphics.export;

import fr.valax.sokoshell.graphics.ActionListener;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Key;
import org.jline.utils.AttributedCharSequence;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.awt.*;

public class TextButton extends Focusable {

    private static final AttributedStyle FOCUSED =
            AttributedStyle.DEFAULT.background(AttributedStyle.CYAN);

    private final AttributedCharSequence text;
    private boolean selected;

    private final AttributedStringBuilder temp = new AttributedStringBuilder();

    public TextButton(String text) {
        this(new AttributedString(text));
    }

    public TextButton(AttributedCharSequence text) {
        this.text = text;
    }

    @Override
    protected void drawComponent(Graphics g) {
        AttributedStyle style = AttributedStyle.DEFAULT;
        if (hasFocus()) {
            style = FOCUSED;
        }

        temp.setLength(0);
        temp.style(style);
        temp.append('[');
        if (selected) {
            temp.append('*');
        } else {
            temp.append(' ');
        }
        temp.append("] ");
        temp.append(text);

        g.getSurface().draw(temp, 0, 0);
    }

    @Override
    protected void updateComponent() {
        if (hasFocus()) {
            if (keyPressed(Key.SPACE)) {
                setSelected(!selected);
            }
        }
    }

    @Override
    protected Dimension compPreferredSize() {
        Insets insets = getInsets();
        Dimension dim = new Dimension();
        dim.width = insets.right + insets.left + 4 + text.columnLength();
        dim.height = insets.top + insets.bottom + 1;

        return dim;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (selected != this.selected) {
            this.selected = selected;

            String command;
            if (selected) {
                command = "selected";
            } else {
                command = "unselected";
            }

            for (ActionListener l : getActionListeners()) {
                l.actionPerformed(this, command);
            }

            repaint();
        }
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