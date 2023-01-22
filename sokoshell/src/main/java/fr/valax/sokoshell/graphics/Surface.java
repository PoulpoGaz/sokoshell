package fr.valax.sokoshell.graphics;

import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Display;

import java.awt.*;
import java.io.PrintStream;
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
        boolean widthChanged = this.width != width;
        boolean heightChanged = this.height != height;

        if (width < 0 || height < 0 || (!widthChanged && !heightChanged)) {
            return;
        }

        if (height != this.height) {
            this.builders = Arrays.copyOf(builders, height);

            for (int y = this.height; y < height; y++) {
                builders[y] = new AttributedStringBuilder();
            }
        }

        // if the width changed, we need to grow or reduce every builder
        int y = widthChanged ? 0 : this.height;
        for (; y < height; y++) {
            if (builders[y].length() > width) {
                builders[y].setLength(width);
            } else {
                while (builders[y].length() != width) {
                    builders[y].append(background);
                }
            }
        }

        this.width = width;
        this.height = height;
    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream out) {
        for (AttributedStringBuilder b : builders) {
            out.println(TrueColorString.toAnsi(b));
        }
    }

    public void print(Terminal terminal) {
        for (AttributedStringBuilder b : builders) {
            terminal.writer().println(TrueColorString.toAnsi(terminal, b));
        }
    }

    /**
     * Draw the surface on the display and put the cursor at cursorPos
     * @param display the display to draw on
     * @param cursorPos cursor position
     */
    public void drawBuffer(Display display, int cursorPos) {
        display.update(asList(), cursorPos);
    }


    public List<AttributedString> asList() {
        List<AttributedString> strings = new ArrayList<>();

        for (AttributedStringBuilder b : builders) {
            strings.add(new TrueColorString(b));
        }

        return strings;
    }

    public AttributedString asString() {
        AttributedStringBuilder asb = new AttributedStringBuilder();

        for (AttributedStringBuilder a : this.builders) {
            asb.append(a).append('\n');
        }

        return asb.toAttributedString();
    }

    /**
     * Clear the buffer.
     * It doesn't clear the screen
     */
    public void clear() {
        for (int y = 0; y < height; y++) {
            builders[y].setLength(0);

            for (int x = 0; x < width; x++) {
                builders[y].append(background);
            }
        }
    }

    /**
     * Draw at (x, y) the char c
     * @param c the char to draw
     * @param x destination x
     * @param y destination y
     */
    public void draw(char c, AttributedStyle style, int x, int y) {
        draw(new AttributedString(String.valueOf(c), style), x, y);
    }

    /**
     * Draw the {@linkplain CharSequence#subSequence(int, int) subsequence} of
     * the string from start to end at (x; y).
     *
     * @param str the string to draw
     * @param x destination x
     * @param y destination y
     */
    public void draw(CharSequence str, int x, int y) {
        draw(str, 0, str.length(), x, y, str.length());
    }

    /**
     * Draw the string at (x; y).
     *
     * @param str the string to draw
     * @param start start index in str (inclusive)
     * @param end end index in str (exclusive)
     * @param x destination x
     * @param y destination y
     */
    public void draw(CharSequence str, int start, int end, int x, int y) {
        draw(str, start, end, x, y, str.length());
    }

    public void draw(CharSequence str, AttributedStyle style, int x, int y) {
        draw(new AttributedString(str, style), x, y);
    }

    public void draw(CharSequence str, AttributedStyle style, int start, int end, int x, int y) {
        draw(new AttributedString(str, style), start, end, x, y);
    }

    /**
     * Draw the string at (x; y).
     * @param str the string to draw
     * @param x destination x
     * @param y destination y
     */
    public void draw(AttributedString str, int x, int y) {
        draw(str, 0, str.length(), x, y, str.columnLength());
    }

    /**
     * Draw the {@linkplain CharSequence#subSequence(int, int) subsequence} of
     * the string from start to end at (x; y).
     *
     * @param str the string to draw
     * @param start start index in str (inclusive)
     * @param end end index in str (exclusive)
     * @param x destination x
     * @param y destination y
     */
    public void draw(AttributedString str, int start, int end, int x, int y) {
        draw(str, start, end, x, y, str.columnLength());
    }

    /**
     * Draw the subsequence of {@code str} from start (inclusive) to end (exclusive) at (x; y).
     *
     * @param str the string to draw
     * @param start start index in str (inclusive)
     * @param end end index in str (exclusive)
     * @param x destination x
     * @param y destination y
     * @param columnLength the length of the string in column between start and end
     */
    private void draw(CharSequence str, int start, int end, int x, int y, int columnLength) {
        if (end - start != columnLength) {
            throw new IllegalArgumentException("Attempting to draw a string that contains non 1-column-length char");
        }

        int origDrawX = x + tx;
        int drawY = y + ty;
        int origEndX = origDrawX + columnLength;

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

        drawUnchecked(str, drawX, drawY, start + drawX - origDrawX, start + endX - origDrawX);
    }

    /**
     * Draw the string at (x; y) with checking if the text can fill in the screen
     *
     * @param str the string to draw
     * @param x destination x
     * @param y destination y
     */
    private void drawUnchecked(CharSequence str, int x, int y, int start, int end) {
        AttributedStringBuilder b = builders[y];

        b.setLength(x);
        b.append(str, start, end);
        b.setLength(width);
    }

    public AttributedString get(int x, int y) {
        int translatedX = x + tx;
        int translatedY = y + ty;

        if (translatedX < clip.x || translatedY < clip.y ||
                translatedX >= clip.x + clip.width || translatedY >= clip.y + clip.height) {
            return null;
        }

        return builders[translatedY].subSequence(x, x + 1);
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
        Rectangle temp = new Rectangle(tx + x, ty + y, width, height);
        clip.setBounds(clip.intersection(temp));
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
}
