package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A class used to draw images and shapes.
 * It uses a {@link Paint} object to draw characters.
 * The default paint supports style (ansi code) and character changing.
 * When invoking {@link #setChar(char)} or {@link #setStyle(AttributedStyle)} the
 * default paint is automatically set.
 */
public class Graphics {

    private final Surface surface;
    private final ColorPaint colorPaint;
    private Paint paint;

    public Graphics(Surface surface) {
        this.surface = surface;
        colorPaint = new ColorPaint();
        paint = colorPaint;
    }

    // * LINES *

    /**
     * Draw a horizontal line from (x; y) to (x + width; y)
     * @param x x
     * @param y y
     * @param width width of the line
     */
    public void drawHorizLine(int x, int y, int width) {
        Rectangle box = new Rectangle(x, y, width, 1);
        surface.draw(paint.fromTo(x, y, width, box), x, y);
    }

    /**
     * Draw a line from (x0; y0) to (x1; y1)
     * @param x0 starting x
     * @param y0 starting y
     * @param x1 ending x
     * @param y1 ending y
     */
    public void drawLine(int x0, int y0, int x1, int y1) {
        if (y0 == y1) {
            if (x0 > x1) {
                drawHorizLine(x1, y0, x0 - x1 + 1);
            } else {
                drawHorizLine(x0, y0, x1 - x0 + 1);
            }
        }

        int w = Math.abs(x1 - x0);
        int h = Math.abs(y1 - y0);

        if (h < w) {
            if (x0 > x1) {
                drawLineLow(x1, y1, x0, y0, w, h);
            } else {
                drawLineLow(x0, y0, x1, y1, w, h);
            }
        } else {
            if (y0 > y1) {
                drawLineHigh(x1, y1, x0, y0, w, h);
            } else {
                drawLineHigh(x0, y0, x1, y1, w, h);
            }
        }
    }

    private void drawLineLow(int x0, int y0, int x1, int y1, int w, int h) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int yi = 1;

        Rectangle box;
        if (dy < 0) {
            yi = -1;
            dy = -dy;

            box = new Rectangle(x0, y1, w, h);
        } else {
            box = new Rectangle(x0, y0, w, h);
        }

        int D = 2 * dy - dx;
        int y = y0;

        for (int x = x0; x <= x1; x++) {
            surface.draw(paint.at(x, y, box), x, y);

            if (D > 0) {
                y += yi;
                D += 2 * (dy - dx);
            } else {
                D += 2 * dy;
            }
        }
    }

    private void drawLineHigh(int x0, int y0, int x1, int y1, int w, int h) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int xi = 1;

        Rectangle box;
        if (dx < 0) {
            xi = -1;
            dx = -dx;

            box = new Rectangle(x1, y0, w, h);
        } else {
            box = new Rectangle(x0, y0, w, h);
        }

        int D = 2 * dx - dy;
        int x = x0;

        for (int y = y0; y <= y1; y++) {
            surface.draw(paint.at(x, y, box), x, y);

            if (D > 0) {
                x += xi;
                D += 2 * (dx - dy);
            } else {
                D += 2 * dx;
            }
        }
    }

    /**
     * Fill a rectangle at (x; y) with size (width; height)
     * @param x x
     * @param y y
     * @param width width
     * @param height height
     */
    public void fillRectangle(int x, int y, int width, int height) {
        Rectangle box = new Rectangle(x, y, width, height);
        int endX = x + width;

        int xClamp = x;
        int widthClamp = width;

        if (x < 0) {
            xClamp = 0;

            if (endX >= surface.getWidth()) {
                widthClamp = surface.getWidth();
            }
        } else if (endX >= surface.getWidth()) {
            widthClamp = width - (endX - surface.getWidth());
        }

        int yLoop = Math.max(y, 0);
        int yEnd = Math.min(y + height, surface.getHeight());
        for (; yLoop < yEnd; yLoop++) {
            AttributedString str = paint.fromTo(xClamp, yLoop, widthClamp, box);
            surface.draw(str, xClamp, yLoop);
        }
    }

    /**
     * Draw a rectangle with border equals to 1
     * at (x; y) with size (width; height)
     * @param x x
     * @param y y
     * @param width width
     * @param height height
     */
    public void drawRectangle(int x, int y, int width, int height) {
        int x2 = x + width - 1;
        int y2 = y + height - 1;

        drawHorizLine(x, y, width - 1);
        drawHorizLine(x, y2, width - 1);

        drawLine(x, y, x, y2);
        drawLine(x2, y, x2, y2);
    }

    /**
     * Fill a circle with center (cx; cy) and radius (radius)
     * @param cx center x
     * @param cy center y
     * @param radius radius of the circle
     */
    public void fillCircle(int cx, int cy, int radius) {
        Rectangle box = new Rectangle(cx - radius, cy - radius,
                radius * 2, radius * 2);

        int squared = radius * radius;

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {

                if (x * x + y * y <= squared) {
                    int drawX = x + cx;
                    int drawY = y + cy;

                    surface.set(paint.at(cx, cy, box), drawX, drawY);
                }

            }
        }
    }

    /**
     * Draw a circle with center (cx; cy) and radius (radius)
     * @param cx center x
     * @param cy center y
     * @param radius radius of the circle
     */
    public void drawCircle(int cx, int cy, int radius) {
        Rectangle box = new Rectangle(cx - radius, cy - radius,
                radius * 2, radius * 2);

        int squared = radius * radius;

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {

                if (Math.abs(x * x + y * y - squared) <= 4) {
                    int drawX = x + cx;
                    int drawY = y + cy;

                    surface.set(paint.at(cx, cy, box), drawX, drawY);
                }

            }
        }
    }

    /**
     * Draw an image at (x; y).
     * If the terminal support true color, there will be no color problem.
     * Otherwise, it uses the current terminal colors.
     * @param image the image to draw
     * @param x x
     * @param y y
     */
    public void drawImage(BufferedImage image, int x, int y) {
        int endX = Math.min(image.getWidth() + x, surface.getWidth());
        int endY = Math.min(image.getHeight() + y, surface.getHeight());

        int yImg = 0;

        for (int y2 = y; y2 < endY; y2++, yImg++) {

            int xImg = 0;
            for (int x2 = x; x2 < endX; x2++, xImg++) {

                int rgb = image.getRGB(xImg, yImg);
                int alpha = (rgb >> 24) & 0xFF;

                if (alpha < 255) {
                    continue;
                }
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                surface.set(' ', AttributedStyle.DEFAULT.background(red, green, blue),
                        x2,
                        y2);
            }
        }
    }

    /**
     * Set the paint to colorPaint and set colorPaint character
     * @param c c
     */
    public void setChar(char c) {
        colorPaint.setChar(c);
        setPaint(colorPaint);
    }

    /**
     * @return color paint character
     */
    public char getChar() {
        return colorPaint.getChar();
    }

    /**
     * Set the paint to colorPaint and set colorPaint style
     * @param style character style
     */
    public void setStyle(AttributedStyle style) {
        colorPaint.setStyle(style);
        setPaint(colorPaint);
    }

    /**
     * @return color paint style
     */
    public AttributedStyle getStyle() {
        return colorPaint.getStyle();
    }

    /**
     * Set the new paint
     * @param paint the new paint
     */
    public void setPaint(Paint paint) {
        if (paint != null) {
            this.paint = paint;
        }
    }

    /**
     * @return The current paint
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * Revert to color paint
     */
    public void setDefaultPaint() {
        setPaint(colorPaint);
    }

    /**
     * @return The surface on which we are drawing
     */
    public Surface getSurface() {
        return surface;
    }

    /**
     * A paint consists of two methods. The first returns
     * the character to draw at a given location and the second
     * returns all character to draw a horizontal line
     */
    public interface Paint {

        /**
         * @param x x
         * @param y y
         * @param box where we are drawing
         * @return the character to draw at (x; y)
         */
        AttributedString at(int x, int y, Rectangle box);

        /**
         * @param x x
         * @param y y
         * @param width width
         * @param box where we are drawing
         * @return character to draw at (x; y) to (x + width; y)
         */
        AttributedString fromTo(int x, int y, int width, Rectangle box);
    }

    /**
     * The default paint
     */
    public static class ColorPaint implements Paint {

        private char c;
        private AttributedStyle style;

        private AttributedString str;

        public ColorPaint() {
            c = ' ';
            style = AttributedStyle.DEFAULT.background(AttributedStyle.BLACK);
        }

        @Override
        public AttributedString at(int x, int y, Rectangle box) {
            return getOrCreate();
        }

        @Override
        public AttributedString fromTo(int x, int y, int width, Rectangle box) {
            AttributedStringBuilder builder = new AttributedStringBuilder();

            AttributedString str = getOrCreate();
            for (int i = 0; i < width; i++) {
                builder.append(str);
            }

            return builder.toAttributedString();
        }

        private AttributedString getOrCreate() {
            if (str == null) {
                str = new AttributedString(String.valueOf(c), style);

                if (str.columnLength() != 1){
                    throw new IllegalStateException("char must be 1 in length");
                }
            }

            return str;
        }

        public char getChar() {
            return c;
        }

        public void setChar(char c) {
            this.c = c;
            getOrCreate();
        }

        public AttributedStyle getStyle() {
            return style;
        }

        public void setStyle(AttributedStyle style) {
            this.style = style;
            str = null;
        }
    }
}
