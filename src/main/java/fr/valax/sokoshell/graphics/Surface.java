package fr.valax.sokoshell.graphics;

import fr.valax.graph.ScatterPlotPoint;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class used by {@link TerminalEngine} to draw.
 * It doesn't support character with column with not equal to 1.
 *
 * You can draw text at any position in the terminal. Drawing at
 * a (x, y) fills at left with "background char". It doesn't erase
 * previously drawn text.
 *
 * @see org.jline.utils.WCWidth
 */
public class Surface {

    private AttributedStringBuilder[] builders;

    private int width = 0;
    private int height = 0;

    private AttributedString background = new AttributedString(" ");

    private int tx;
    private int ty;
    private Rectangle clip = new Rectangle(0, 0, 65536, 65536);

    public Surface() {
        builders = new AttributedStringBuilder[0];
    }

    /**
     * Resize the buffered
     * @param size new size
     */
    public void resize(Size size) {
        resize(size.getColumns(), size.getRows());
    }

    /**
     * Resize the buffer to width; height
     * @param width new width
     * @param height new height
     */
    public void resize(int width, int height) {
        if (width < 0 || height < 0) {
            return;
        }

        if (height != this.height) {
            this.builders = Arrays.copyOf(builders, height);

            for (int y = this.height; y < height; y++) {
                builders[y] = new AttributedStringBuilder();
            }
        }

        this.width = width;
        this.height = height;
    }

    /**
     * Draw the surface on the display and put the cursor at cursorPos
     * @param display the display to draw on
     * @param cursorPos cursor position
     */
    public void drawBuffer(Display display, int cursorPos) {
        List<AttributedString> strings = new ArrayList<>();

        for (AttributedStringBuilder b : builders) {
            strings.add(new TrueColorString(b.toAttributedString()));
        }

        display.update(strings, cursorPos);
    }

    public void drawBuffer() {
        drawBuffer(System.out);
    }

    public void drawBuffer(PrintStream out) {
        for (AttributedStringBuilder b : builders) {
            out.println(new TrueColorString(b.toAttributedString()).toAnsi());
        }
    }

    /**
     * Clear the buffer.
     * It doesn't clear the screen
     */
    public void clear() {
        for (AttributedStringBuilder builder : builders) {
            builder.setLength(0);
        }
    }

    /**
     * Set at (x, y) the string. It should be a one-column length string
     * @param str the string to draw
     * @param x destination x
     * @param y destination y
     */
    public void set(AttributedString str, int x, int y) {
        if (str.columnLength() == 1) {
            draw(str, x, y);
        } else if (str.columnLength() > 1) {
            AttributedString sub = str.subSequence(0, 1);

            if (sub.columnLength() > 1) {
                throw new IllegalArgumentException();
            }

            draw(sub, x, y);
        }
    }

    /**
     * Set at (x, y) the char c
     * @param c the char to draw
     * @param x destination x
     * @param y destination y
     */
    public void set(char c, AttributedStyle style, int x, int y) {
        draw(new AttributedString(String.valueOf(c), style), x, y);
    }

    /**
     * Draw the string at (x; y).
     * @param str the string to draw
     * @param x destination x
     * @param y destination y
     */
    public void draw(String str, int x, int y) {
        draw(new AttributedString(str), x, y);
    }

    /**
     * Draw the string at (x; y).
     * @param str the string to draw
     * @param x destination x
     * @param y destination y
     */
    public void draw(AttributedString str, int x, int y) {
        int len = str.columnLength();

        if (str.length() != str.columnLength()) {
            throw new IllegalArgumentException("Attempting to draw a string that contains non 1-column-length char");
        }

        int origDrawX = x + tx;
        int drawY = y + ty;
        int origEndX = origDrawX + len;

        if (drawY < clip.y || drawY >= clip.y + clip.height || drawY < 0 || drawY >= height) {
            return;
        }

        int drawX = origDrawX;
        int endX = origEndX;

        int minX = Math.max(clip.x, 0);
        if (drawX < minX) {
            drawX = minX;
        }

        int maxX = Math.min(width, clip.x + clip.width);
        if (endX >= maxX) {
            endX = maxX;
        }

        if (drawX >= endX) {
            return;
        }

        try {
            drawUnchecked(str.substring(drawX - origDrawX, endX - origDrawX), drawX, drawY);
        } catch (IndexOutOfBoundsException | InvalidParameterException e) {
            System.out.println(str);
            System.out.println(origDrawX + " - " + origEndX);
            System.out.println(drawX + " - " + endX);
            System.out.println((drawX - origDrawX + " - " + (endX - drawX)));
            System.out.println(clip);
            System.out.println(tx + " - " + ty);
            e.printStackTrace();

            System.exit(0);
        }
    }

    /**
     * Draw the string at (x; y) with checking if the text can fill in the screen
     * @param str the string to draw
     * @param x destination x
     * @param y destination y
     */
    private void drawUnchecked(AttributedString str, int x, int y) {
        AttributedStringBuilder b = builders[y];

        int endX = x + str.length();

        int length = -1;
        if (b.length() < x) {
            for (int x2 = b.length(); x2 < x; x2++) {
                b.append(background);
            }
        } else if (b.length() > x) {
            if (b.length() > endX) {
                length = b.length();
            }

            b.setLength(x);
        }
        b.append(str);

        if (length >= 0) {
            b.setLength(length);
        }
    }

    /**
     * @return the width of the buffer
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of the buffer
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the background char
     */
    public AttributedString getBackground() {
        return background;
    }

    /**
     * Set the new background string. It should be a one-column length string
     * @param background new background
     */
    public void setBackground(AttributedString background) {
        if (background != null && background.columnLength() == 1) {
            this.background = background;
        }
    }

    public void translate(int tx, int ty) {
        this.tx += tx;
        this.ty += ty;
    }

    public void setTranslation(int x, int y) {
        tx = x;
        ty = y;
    }

    public int getTx() {
        return tx;
    }

    public int getTy() {
        return ty;
    }

    /**
     * set clip. clip are translated
     */
    public void setClip(int x, int y, int width, int height) {
        clip.setBounds(tx + x, ty + y, width, height);
    }

    /**
     * intersect clips. clip are translated
     */
    public void intersectClip(int x, int y, int width, int height) {
        clip.setBounds(clip.intersection(tx + x, ty + y, width, height));
    }

    public void setClip(Rectangle clip) {
        this.clip.setBounds(clip);
    }

    public Rectangle getClip() {
        return new Rectangle(clip);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < builders.length; i++) {
            AttributedStringBuilder asb = builders[i];
            sb.append(asb.toAnsi());

            if (i + 1 < builders.length) {
                sb.append('\n');
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        try (Terminal terminal = TerminalBuilder.terminal()) {

            try (TestClass test = new TestClass(terminal)) {
                test.loop();
            } finally {
                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.writer().flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private enum KeyEvent {
        UP,
        DOWN
    }

    /**
     * A test class
     */
    private static class TestClass extends TerminalEngine<KeyEvent> {

        private Surface surface;
        private Graphics g;
        private BufferedImage img;

        public TestClass(Terminal terminal) {
            super(terminal);
        }

        @Override
        protected void init() {
            surface = new Surface();
            surface.resize(terminal.getWidth(), terminal.getHeight());

            g = new Graphics(surface);
            try {
                img = ImageIO.read(new File("styles/isekai/tileset.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            keyMap.bind(KeyEvent.DOWN, KeyMap.key(terminal, InfoCmp.Capability.key_up));
            keyMap.bind(KeyEvent.UP, KeyMap.key(terminal, InfoCmp.Capability.key_up));
        }

        @Override
        protected int render(Size size) {
            surface.clear();
            surface.setClip(0, 0, size.getColumns(), size.getRows());
            surface.draw(AttributedString.fromAnsi("hello world!"), 5, 5);
            surface.draw(AttributedString.fromAnsi("oooooooorld!"), 13, 5);
            surface.draw(AttributedString.fromAnsi("aqzsedrftgHello world!"), -10, 0);

            g.setStyle(AttributedStyle.DEFAULT.background(AttributedStyle.GREEN));
            g.fillCircle(50, 10, 10);

            g.setStyle(AttributedStyle.DEFAULT.background(AttributedStyle.RED));
            g.drawCircle(50, 10, 5);

            g.setChar('c');
            g.setStyle(AttributedStyle.DEFAULT.blink());

            int cx = 15;
            int cy = 25;
            int rad = 10;

            g.drawLine(cx, cy, cx - rad, cy);
            g.drawLine(cx, cy, cx, cy - rad);
            g.drawLine(cx, cy, cx - rad, cy - rad);

            g.drawLine(cx, cy, cx + rad, cy);
            g.drawLine(cx, cy, cx, cy + rad);
            g.drawLine(cx, cy, cx + rad, cy + rad);

            g.drawLine(cx, cy, cx - rad, cy + rad);
            g.drawLine(cx, cy, cx + rad, cy - rad);

            g.drawLine(0, 0, surface.getWidth(), surface.getHeight());

            surface.translate(100, 10);
            g.setChar(' ');
            g.setStyle(AttributedStyle.DEFAULT.background(AttributedStyle.YELLOW));
            g.fillRectangle(0, 0, 50, 10);

            surface.translate(10, 2);
            g.setStyle(AttributedStyle.DEFAULT.background(AttributedStyle.MAGENTA));
            g.drawRectangle(0, 0, 30, 6);
            surface.translate(-110, -12);

            surface.draw(new AttributedString("FPS: " + getFPS()), 0, surface.getHeight() - 2);
            surface.draw(new AttributedString("TPS: " + getTPS()), 0, surface.getHeight() - 1);
            g.drawImage(img, 50, 20);

            surface.translate(0, 40);

            Rectangle old = surface.getClip();
            surface.setClip(0, 0, 10, 20);
            g.setPaint(new RadialGradient(java.awt.Color.RED, java.awt.Color.BLUE));
            g.fillRectangle(0, 0, 20, 20);
            surface.setClip(old);
            surface.translate(0, -40);

            return 0;
        }

        @Override
        protected void update() {
            surface.resize(terminal.getWidth(), terminal.getHeight());
        }
    }

    /**
     * From box center
     */
    private static class RadialGradient implements Graphics.Paint {

        private final java.awt.Color from;
        private final java.awt.Color to;

        public RadialGradient(java.awt.Color from, java.awt.Color to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public AttributedString at(int x, int y, Rectangle box) {
            double x2 = x - box.x;
            double y2 = y - box.y;

            double cx = box.width / 2d;
            double cy = box.height / 2d;

            double gradRadius = Math.sqrt(cx * cx + cy * cy);
            double radius = Math.sqrt((x2 - cx) * (x2 - cx) + (y2 - cy) * (y2 - cy));

            double factor = radius / gradRadius;

            int newRed   = (int) ((1 - factor) * from.getRed() + factor * to.getRed());
            int newGreen = (int) ((1 - factor) * from.getGreen() + factor * to.getGreen());
            int newBlue  = (int) ((1 - factor) * from.getBlue() + factor * to.getBlue());

            return new AttributedString(" ", AttributedStyle.DEFAULT.background(newRed, newGreen, newBlue));
        }

        @Override
        public AttributedString fromTo(int x, int y, int width, Rectangle box) {
            AttributedStringBuilder builder = new AttributedStringBuilder();

            for (int x2 = x; x2 <= x + width; x2++) {
                builder.append(at(x2, y, box));
            }

            return builder.toAttributedString();
        }
    }
}
