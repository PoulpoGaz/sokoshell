package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.awt.image.BufferedImage;

public class Graphics {

    private final Surface surface;
    private final StandardPaint standardPaint;
    private Paint paint;

    public Graphics(Surface surface) {
        this.surface = surface;
        standardPaint = new StandardPaint();
        paint = standardPaint;
    }

    // * LINES *

    public void drawHorizLine(int x, int y, int width) {
        surface.draw(paint.fromTo(x, y, width), x, y);
    }

    public void drawLine(int x0, int y0, int x1, int y1) {
        if (y0 == y1) {
            if (x0 > x1) {
                drawHorizLine(x1, y0, x0 - x1 + 1);
            } else {
                drawHorizLine(x0, y0, x1 - x0 + 1);
            }
        }

        if (Math.abs(y1 - y0) < Math.abs(x1 - x0)) {
            if (x0 > x1) {
                drawLineLow(x1, y1, x0, y0);
            } else {
                drawLineLow(x0, y0, x1, y1);
            }
        } else {
            if (y0 > y1) {
                drawLineHigh(x1, y1, x0, y0);
            } else {
                drawLineHigh(x0, y0, x1, y1);
            }
        }
    }

    private void drawLineLow(int x0, int y0, int x1, int y1) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int yi = 1;

        if (dy < 0) {
            yi = -1;
            dy = -dy;
        }

        int D = 2 * dy - dx;
        int y = y0;

        for (int x = x0; x <= x1; x++) {
            surface.draw(paint.at(x, y), x, y);

            if (D > 0) {
                y += yi;
                D += 2 * (dy - dx);
            } else {
                D += 2 * dy;
            }
        }
    }

    private void drawLineHigh(int x0, int y0, int x1, int y1) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int xi = 1;

        if (dx < 0) {
            xi = -1;
            dx = -dx;
        }

        int D = 2 * dx - dy;
        int x = x0;

        for (int y = y0; y <= y1; y++) {
            surface.draw(paint.at(x, y), x, y);

            if (D > 0) {
                x += xi;
                D += 2 * (dx - dy);
            } else {
                D += 2 * dx;
            }
        }
    }

    public void fillRectangle(int x, int y, int width, int height) {
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
            AttributedString str = paint.fromTo(xClamp, yLoop, widthClamp);
            surface.draw(str, xClamp, yLoop);
        }
    }

    public void drawRectangle(int x, int y, int width, int height) {
        int x2 = x + width - 1;
        int y2 = y + height - 1;

        drawHorizLine(x, y, width - 1);
        drawHorizLine(x, y2, width - 1);

        drawLine(x, y, x, y2);
        drawLine(x2, y, x2, y2);
    }

    public void fillCircle(int cx, int cy, int radius) {
        int squared = radius * radius;

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {

                if (x * x + y * y <= squared) {
                    int drawX = x + cx;
                    int drawY = y + cy;

                    surface.set(paint.at(cx, cy), drawX, drawY);
                }

            }
        }
    }

    public void drawCircle(int cx, int cy, int radius) {
        int squared = radius * radius;

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {

                if (Math.abs(x * x + y * y - squared) <= 4) {
                    int drawX = x + cx;
                    int drawY = y + cy;

                    surface.set(paint.at(cx, cy), drawX, drawY);
                }

            }
        }
    }

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

    public void setChar(char c) {
        standardPaint.setChar(c);
        setPaint(standardPaint);
    }

    public char getChar() {
        return standardPaint.getChar();
    }

    public void setStyle(AttributedStyle style) {
        standardPaint.setStyle(style);
        setPaint(standardPaint);
    }

    public AttributedStyle getStyle() {
        return standardPaint.getStyle();
    }

    public void setPaint(Paint paint) {
        if (paint != null) {
            this.paint = paint;
        }
    }

    public Paint getPaint() {
        return paint;
    }

    public Surface getSurface() {
        return surface;
    }

    public interface Paint {

        AttributedString at(int x, int y);

        AttributedString fromTo(int x, int y, int width);
    }

    public static class StandardPaint implements Paint {

        private char c;
        private AttributedStyle style;

        private AttributedString str;

        public StandardPaint() {
            c = ' ';
            style = AttributedStyle.DEFAULT.background(AttributedStyle.BLACK);
        }

        @Override
        public AttributedString at(int x, int y) {
            return getOrCreate();
        }

        @Override
        public AttributedString fromTo(int x, int y, int width) {
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
