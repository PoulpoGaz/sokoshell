package fr.valax.sokoshell.graphics;

import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class used by {@link View} to draw.
 * It doesn't support character with column with not equal to 1
 *
 * @see org.jline.utils.WCWidth
 */
public class Surface {

    private AttributedStringBuilder[] builders;

    private int width = 0;
    private int height = 0;

    private AttributedString background = new AttributedString(" ");

    public Surface() {
        builders = new AttributedStringBuilder[0];
    }

    public void resize(Size size) {
        resize(size.getColumns(), size.getRows());
    }

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

    public void drawBuffer(Display display, int cursorPos) {
        List<AttributedString> strings = new ArrayList<>();

        for (AttributedStringBuilder b : builders) {
            strings.add(new TrueColorString(b.toAttributedString()));
        }

        display.update(strings, cursorPos);
    }

    public void clear() {
        for (AttributedStringBuilder builder : builders) {
            builder.setLength(0);
        }
    }

    public void set(AttributedString str, int x, int y) {
        if (str.columnLength() == 1) {
            draw(str, x, y);
        } else if (str.columnLength() > 1) {
            set(str.substring(0, 1), x, y);
        }
    }

    public void set(char c, AttributedStyle style, int x, int y) {
        draw(new AttributedString(String.valueOf(c), style), x, y);
    }

    public void draw(String str, int x, int y) {
        draw(new AttributedString(str), x, y);
    }

    public void draw(AttributedString str, int x, int y) {
        if (str.length() != str.columnLength()) {
            throw new IllegalArgumentException("Attempting to draw a string that contains non 1-column-length char");
        }

        int endX = x + str.columnLength();

        if (y < 0 || y >= height || endX < 0 || x >= width) {
            return;
        }

        int drawX = x;
        int start = 0;
        if (x < 0) {
            start = -x;
            drawX = 0;
        }

        int end = str.length();
        if (endX > width) {
            end = end - (endX - width);
        }

        drawUnchecked(str.substring(start, end), drawX, y);
    }

    /**
     * Doesn't check if the text can fill in the screen
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

    private boolean outside(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public AttributedString getBackground() {
        return background;
    }

    public void setBackground(AttributedString background) {
        if (background != null && background.columnLength() == 1) {
            this.background = background;
        }
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

    private static class TestClass extends View<KeyEvent> {

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
                img = ImageIO.read(TestClass.class.getResourceAsStream("/tileset.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            keyMap.bind(KeyEvent.DOWN, KeyMap.key(terminal, InfoCmp.Capability.key_up));
            keyMap.bind(KeyEvent.UP, KeyMap.key(terminal, InfoCmp.Capability.key_up));
        }

        @Override
        protected void render(Size size) {
            surface.clear();
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

            g.setChar(' ');
            g.setStyle(AttributedStyle.DEFAULT.background(AttributedStyle.YELLOW));
            g.fillRectangle(100, 10, 50, 10);

            g.setStyle(AttributedStyle.DEFAULT.background(AttributedStyle.MAGENTA));
            g.drawRectangle(110, 12, 30, 6);

            surface.draw(new AttributedString("FPS: " + getFPS()), 0, surface.getHeight() - 2);
            surface.draw(new AttributedString("TPS: " + getTPS()), 0, surface.getHeight() - 1);
            g.drawImage(img, 50, 20);


            surface.drawBuffer(display, 0);
        }

        @Override
        protected void update() {
            surface.resize(terminal.getWidth(), terminal.getHeight());
        }
    }
}
