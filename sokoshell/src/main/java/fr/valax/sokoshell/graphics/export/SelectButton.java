package fr.valax.sokoshell.graphics.export;

import fr.valax.sokoshell.graphics.ActionListener;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Key;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SelectButton extends Focusable {

    private static final AttributedStyle SELECTED = AttributedStyle.DEFAULT;

    private static final AttributedStyle SELECTED_FOCUSED =
            AttributedStyle.DEFAULT.background(AttributedStyle.CYAN);

    private static final AttributedStyle UNSELECTED =
            AttributedStyle.DEFAULT.foreground(AttributedStyle.BLACK + AttributedStyle.BRIGHT);

    private static final AttributedStyle UNSELECTED_FOCUSED =
            UNSELECTED.background(AttributedStyle.CYAN);

    private final List<AttributedString> choices;
    private int choice;

    private final AttributedStringBuilder temp = new AttributedStringBuilder();

    public SelectButton() {
        this.choices = new ArrayList<>();
    }

    @Override
    protected void drawComponent(Graphics g) {
        AttributedStyle style = UNSELECTED;
        if (hasFocus()) {
            style = UNSELECTED_FOCUSED;
        }

        temp.setLength(0);

        for (int i = 0; i < choices.size(); i++) {
            AttributedString text = choices.get(i);

            if (i == choice) {
                if (hasFocus()) {
                    temp.append(text, SELECTED_FOCUSED);
                } else {
                    temp.append(text, SELECTED);
                }
            } else {
                temp.append(text, style);
            }

            if (i + 1 < choices.size()) {
                temp.append(" | ", style);
            }
        }

        g.getSurface().draw(temp, 0, 0);
    }

    @Override
    protected void updateComponent() {
        if (hasFocus()) {
            if (keyPressed(Key.SPACE)) {
                setChoice(choice + 1);
            }
        }
    }

    @Override
    protected Dimension compPreferredSize() {
        Insets insets = getInsets();
        Dimension dim = new Dimension();
        dim.width = insets.right + insets.left;
        dim.height = insets.top + insets.bottom + 1;

        for (int i = 0; i < choices.size(); i++) {
            AttributedString line = choices.get(i);
            dim.width += line.columnLength();

            if (i + 1 < choices.size()) {
                dim.width += 3;
            }
        }

        return dim;
    }

    public void addChoice(String text) {
        choices.add(new AttributedString(text));
    }

    public void setChoice(int i) {
        if (choices.isEmpty()) {
            return;
        }

        int choice = Math.floorMod(i, choices.size());
        if (choice != this.choice) {
            this.choice = choice;
            repaint();

            for (ActionListener l : getActionListeners()) {
                l.actionPerformed(this, "choice");
            }
        }
    }

    public int getChoiceIndex() {
        return choice;
    }

    public AttributedString getChoice() {
        if (choices.isEmpty()) {
            return null;
        }

        return choices.get(choice);
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