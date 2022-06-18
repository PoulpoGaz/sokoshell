package fr.valax.graph;

import java.awt.*;

public class Label {

    private static final Toolkit toolkit = Toolkit.getDefaultToolkit();

    private String text;
    private Font font = Utils.DISCORD_FONT;
    private Color color = Utils.TEXT_COLOR;

    private Insets insets;

    private Dimension preferredSize;

    public Label(String text) {
        this.text = text;
        insets = null;
    }

    @SuppressWarnings("deprecation")
    public Dimension preferredSize() {
        if (preferredSize == null) {
            FontMetrics fm = toolkit.getFontMetrics(font);

            preferredSize = new Dimension();

            if (text != null) {
                preferredSize.width = fm.stringWidth(text);
                preferredSize.height = fm.getHeight();
            }

            if (insets != null) {
                preferredSize.width += insets.left + insets.right;
                preferredSize.height += insets.top + insets.bottom;
            }
        }

        return preferredSize;
    }

    public void draw(Graphics2D g2d, Dimension size) {
        if (text == null) {
            return;
        }

        int x = 0;
        int y = 0;

        if (insets != null) {
            size.width = size.width - insets.left - insets.right;
            size.height = size.height - insets.top - insets.bottom;

            x = insets.left;
            y = insets.top;
        }

        if (color != null) {
            g2d.setColor(color);
        }

        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();

        x += (size.width - fm.stringWidth(text)) / 2;
        y += (size.height - fm.getHeight()) / 2 + fm.getAscent();

        g2d.drawString(text, x, y);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text != null && !text.equals(this.text)) {
            this.text = text;
            preferredSize = null;
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Insets getInsets() {
        return insets;
    }

    public void setInsets(Insets insets) {
        if (this.insets != null && !this.insets.equals(insets)) {
            this.insets = insets;
            preferredSize = null;
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font != null && !this.font.equals(font)) {
            this.font = font;

            preferredSize = null;
        }
    }
}