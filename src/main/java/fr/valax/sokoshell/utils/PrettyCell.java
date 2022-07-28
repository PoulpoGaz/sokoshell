package fr.valax.sokoshell.utils;

import org.jline.utils.AttributedString;

import java.util.Objects;
import java.util.function.Function;

public class PrettyCell<T> {

    private static final AttributedString NULL = new AttributedString("null");
    private static final AttributedString[] NULL_ARRAY = new AttributedString[] {NULL};

    protected final PrettyColumn<T> column;

    protected int y;

    protected T content;
    protected Alignment xAlignment;
    protected Alignment yAlignment;

    protected AttributedString[] contentAsString;
    protected int width = -1;

    public PrettyCell(int y, PrettyColumn<T> column, T content) {
        this(y, column, content, Alignment.CENTER, Alignment.CENTER);
    }

    public PrettyCell(int y, PrettyColumn<T> column, T content, Alignment xAlignment, Alignment yAlignment) {
        this.y = y;
        this.column = column;
        this.content = content;
        setXAlignment(xAlignment);
        setYAlignment(yAlignment);
    }


    /**
     * Append to the string builder, the y-th line of the cell. It returns
     * how many chars it writes.
     *
     * @param sb the string builder
     * @param cellWidth cell width
     * @param cellHeight cell height
     * @param y the y-th line to append
     * @return how many chars the function writes
     */
    protected int draw(StringBuilder sb, int cellWidth, int cellHeight, int y) {
        int yOffset;

        switch (yAlignment) {
            case UP -> yOffset = y;
            case DOWN -> yOffset = y - height();
            case CENTER -> yOffset = y - (cellHeight - height()) / 2;
            default -> throw new IllegalStateException();
        }

        if (yOffset < 0 || yOffset >= height()) {
            return 0;
        } else {
            return draw(sb, cellWidth, getContentAsString()[yOffset]);
        }
    }

    protected int draw(StringBuilder sb, int cellWidth, AttributedString line) {
        return switch (xAlignment) {
            case CENTER -> {
                int x = (cellWidth - line.columnLength()) / 2;

                sb.append(" ".repeat(x));
                sb.append(line.toAnsi());

                yield x + line.columnLength();
            }
            case LEFT -> {
                sb.append(line.toAnsi());
                yield line.columnLength();
            }
            case RIGHT -> {
                sb.append(" ".repeat(cellWidth - line.columnLength()));
                sb.append(line.toAnsi());

                yield cellWidth;
            }
            default -> throw new IllegalStateException();
        };
    }

    protected int width() {
        if (width < 0) {
            width = 0;

            for (AttributedString str : getContentAsString()) {
                width = Math.max(width, str.columnLength());
            }
        }

        return width;
    }

    protected int height() {
        return getContentAsString().length;
    }


    protected AttributedString[] getContentAsString() {
        if (contentAsString == null) {
            Function<T, AttributedString[]> toString = column.getToString();

            if (toString != null) {
                contentAsString = toString.apply(content);
            }

            if (contentAsString == null) {
                if (content == null) {
                    contentAsString = NULL_ARRAY;
                } else {
                    String[] split = content.toString().split("\n");

                    contentAsString = new AttributedString[split.length];
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        contentAsString[i] = new AttributedString(s);
                    }
                }
            }
        }

        return contentAsString;
    }


    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        if (this.content != content) {
            this.content = content;
            contentAsString = null;
            width = -1;
        }
    }

    public Alignment getxAlignment() {
        return xAlignment;
    }

    public void setXAlignment(Alignment xAlignment) {
        this.xAlignment = Objects.requireNonNullElse(xAlignment, Alignment.CENTER);
    }

    public Alignment getyAlignment() {
        return yAlignment;
    }

    public void setYAlignment(Alignment yAlignment) {
        this.yAlignment = Objects.requireNonNullElse(yAlignment, Alignment.CENTER);
    }

    public int getY() {
        return y;
    }

    static class Header extends PrettyCell<AttributedString[]> {

        public Header(int y, AttributedString[] content) {
            super(y, null, content);
        }

        public Header(int y, AttributedString[] content, Alignment xAlignment, Alignment yAlignment) {
            super(y, null, content, xAlignment, yAlignment);
        }

        @Override
        protected AttributedString[] getContentAsString() {
            contentAsString = content;

            return contentAsString;
        }
    }
}
