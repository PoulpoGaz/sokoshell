package fr.valax.graph;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class Pie<E> {

    private Color background;
    private Color borderColor;
    private int borderSize = -1;

    private Font font;
    private Color fontColor;

    private Map<E, Integer> values;
    private Function<E, String> toString;
    private Function<E, Color> toColor;
    private Function<E, BufferedImage> toImage;

    private Comparator<Map.Entry<E, Integer>> comparator;

    private boolean showPercent = true;
    private int precision = -1;

    public Pie() {

    }

    public BufferedImage create(int width, int height) {
        Objects.requireNonNull(background, "color is null");
        Objects.requireNonNull(font, "font is null");
        Objects.requireNonNull(fontColor, "fontColor is null");
        Objects.requireNonNull(toString, "toString is null");
        Objects.requireNonNull(values, "values is null");

        if (toColor == null && toImage == null) {
            throw new NullPointerException("toColor and toImage are null. One is required.");
        }

        List<Map.Entry<E, Integer>> values = asList(this.values);

        if (values.size() == 0) {
            return null;
        }

        int total = 0;

        for (Map.Entry<E, Integer> pair : values) {
            int v = 0;
            if (pair != null && pair.getValue() != null) {
                v = pair.getValue();
            }
            total += v;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();

        try {
            Utils.setup(g2d, width, height, font, background);

            List<Float> degrees = drawPieBackground(values, g2d, width, height, total);
            drawText(values, g2d, width, height, degrees, total);
        } finally {
            g2d.dispose();
        }

        return image;
    }

    private List<Map.Entry<E, Integer>> asList(Map<E, Integer> map) {
        if (comparator != null) {
            return map.entrySet().stream().sorted(comparator).toList();
        } else {
            return map.entrySet().stream().toList();
        }
    }

    protected List<Float> drawPieBackground(List<Map.Entry<E, Integer>> values, Graphics2D g2d, int width, int height, float total) {
        List<Float> angles = new ArrayList<>(); // angles in degrees

        Arc2D.Float arc = new Arc2D.Float(0, 0, width, height, 0, 0, Arc2D.PIE);

        BasicStroke stroke = null;
        if (borderSize > 0) {
            stroke = new BasicStroke(borderSize);
        }

        Stroke old = g2d.getStroke();
        for (Map.Entry<E, Integer> pair : values) {
            if (pair == null) {
                continue;
            }
            Integer val = pair.getValue();

            if (val == null || val == 0) {
                continue;
            }

            arc.start += arc.extent;
            arc.extent = 360f * val / total;

            if (toColor == null) {
                BufferedImage image = toImage.apply(pair.getKey());
                g2d.setPaint(new TexturePaint(image, arc.getBounds2D()));
            } else {
                Color color = toColor.apply(pair.getKey());
                g2d.setColor(color);
            }


            g2d.fill(arc);

            angles.add(arc.start + arc.extent / 2f);

            if (borderSize > 0 && borderColor != null) {
                g2d.setColor(borderColor);
                g2d.setStroke(stroke);
                g2d.draw(arc);
                g2d.setStroke(old);
            }
        }

        return angles;
    }

    protected void drawText(List<Map.Entry<E, Integer>> values, Graphics2D g2d, int width, int height, List<Float> degrees, int totalInt) {
        BigDecimal total = null;

        if (showPercent) {
            total = BigDecimal.valueOf(totalInt);
        }

        g2d.setColor(fontColor);

        int j = 0;
        for (Map.Entry<E, Integer> pair : values) {
            if (pair == null || pair.getValue() == null || pair.getValue() == 0) {
                continue;
            }

            double radians = Math.toRadians(degrees.get(j));

            float cx = (float) (Math.cos(radians) * width * 0.4 + width * 0.5d);
            float cy = (float) (-Math.sin(radians) * height * 0.4 + height * 0.5d);

            String text = toString.apply(pair.getKey());

            FontMetrics fm = g2d.getFontMetrics();
            if (showPercent && precision >= 0) {
                double percent = Utils.asPercentage(pair.getValue(), total, precision);

                float textCY = cy - fm.getHeight() / 2f;
                float percentCY = cy + fm.getHeight() / 2f;

                drawStringCentered(g2d, fm, text, cx, textCY);
                drawStringCentered(g2d, fm, percent + "%", cx, percentCY);
            } else {
                drawStringCentered(g2d, fm, text, cx, cy);
            }

            j++;
        }
    }

    protected void drawStringCentered(Graphics2D g2d, FontMetrics fm, String text, float cx, float cy) {
        g2d.drawString(text, cx - fm.stringWidth(text) / 2f, cy - fm.getHeight() / 2f + fm.getAscent());
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public Map<E, Integer> getValues() {
        return values;
    }

    public void setValues(Map<E, Integer> values) {
        this.values = values;
    }

    public Function<E, String> getToString() {
        return toString;
    }

    public Function<E, Color> getToColor() {
        return toColor;
    }

    public void setToColor(Function<E, Color> toColor) {
        this.toColor = toColor;
    }

    public Function<E, BufferedImage> getToImage() {
        return toImage;
    }

    public void setToImage(Function<E, BufferedImage> toImage) {
        this.toImage = toImage;
    }

    public void setToString(Function<E, String> toString) {
        this.toString = toString;
    }

    public Comparator<Map.Entry<E, Integer>> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<Map.Entry<E, Integer>> comparator) {
        this.comparator = comparator;
    }

    public boolean isShowPercent() {
        return showPercent;
    }

    public void setShowPercent(boolean showPercent) {
        this.showPercent = showPercent;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }
}