package fr.valax.sokoshell.graphics.export;

import fr.valax.sokoshell.graphics.Component;
import fr.valax.sokoshell.graphics.Graphics;
import org.jline.utils.AttributedString;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MultilineLabel extends Component {

    private final List<AttributedString> lines;

    public MultilineLabel(String text) {
        String[] lines = text.split("\n");

        this.lines = new ArrayList<>(lines.length);
        for (String line : lines) {
            this.lines.add(new AttributedString(line));
        }
    }

    @Override
    protected void drawComponent(Graphics g) {
        for (int y = 0; y < lines.size(); y++) {
            AttributedString line = lines.get(y);
            g.getSurface().draw(line, 0, y);
        }
    }

    @Override
    protected Dimension compPreferredSize() {
        Insets insets = getInsets();
        Dimension dim = new Dimension();
        dim.width = insets.right + insets.left;
        dim.height = insets.top + insets.bottom + lines.size();

        int maxTextWidth = 0;
        for (AttributedString line : lines) {
            maxTextWidth = Math.max(line.columnLength(), maxTextWidth);
        }

        dim.width += maxTextWidth;

        return dim;
    }
}