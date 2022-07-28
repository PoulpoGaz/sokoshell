package fr.valax.sokoshell.utils;

import org.jline.utils.AttributedString;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PrettyTable {

    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT,
        UP,
        DOWN
    }

    public static final class Cell {

        private final Alignment xAlignment;
        private final Alignment yAlignment;
        private final AttributedString[] content;

        private int width = -1;

        public Cell(String content) {
            this(Alignment.CENTER, Alignment.CENTER, content);
        }

        public Cell(AttributedString content) {
            this(Alignment.CENTER, Alignment.CENTER, new AttributedString[] {content});
        }

        public Cell(Alignment xAlignment, Alignment yAlignment, String content) {
            this(xAlignment, yAlignment, content.split(System.lineSeparator()));
        }

        public Cell(Alignment xAlignment, Alignment yAlignment, String[] content) {
            this.xAlignment = Objects.requireNonNull(xAlignment);
            this.yAlignment = Objects.requireNonNull(yAlignment);
            this.content = new AttributedString[content.length];

            for (int i = 0; i < content.length; i++) {
                String str = content[i];
                this.content[i] = new AttributedString(str);
            }
        }

        public Cell(Alignment xAlignment, Alignment yAlignment, AttributedString[] content) {
            this.xAlignment = Objects.requireNonNull(xAlignment);
            this.yAlignment = Objects.requireNonNull(yAlignment);
            this.content = content;

            for (AttributedString str : content) {
                Objects.requireNonNull(str);
            }
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
        private int draw(StringBuilder sb, int cellWidth, int cellHeight, int y) {
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
                return draw(sb, cellWidth, content[yOffset]);
            }
        }

        private int draw(StringBuilder sb, int cellWidth, AttributedString line) {
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

        public int width() {
            if (width < 0) {
                width = 0;

                for (AttributedString str : content) {
                    width = Math.max(width, str.columnLength());
                }
            }

            return width;
        }

        public int height() {
            return content.length;
        }
    }

    /**
     * @param content y - x order
     */
    public static String create(String[] headers, String[][] content) {
        return create(headers.length, content.length,
                (i) -> new Cell(headers[i]),
                (x, y) -> new Cell(content[y][x]));
    }

    public static String create(int width, int height,
                                String[] headers,
                                BiFunction<Integer, Integer, Cell> cellsFunc) {
        return create(width, height,
                (i) -> new Cell(headers[i]),
                cellsFunc);
    }

    public static String create(int width, int height,
                                Function<Integer, Cell> headersFunc,
                                BiFunction<Integer, Integer, Cell> cellsFunc) {
        Cell[] headers = new Cell[width];
        Cell[][] cells = new Cell[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = cellsFunc.apply(x, y);
            }
        }

        for (int i = 0; i < width; i++) {
            headers[i] = headersFunc.apply(i);
        }

        return create(headers, cells);
    }

    /**
     * @param content y - x order
     */
    public static String create(Cell[] headers, Cell[][] content) {
        Objects.requireNonNull(content);

        if (content.length == 0) {
            throw new IllegalArgumentException("No content");
        }

        int tableWidth = headers == null ? content[0].length : headers.length;
        int tableHeight = content.length; // without header

        int[] columnsWidth = getColumnsWidth(headers, content, tableWidth);
        int[] rowsHeight = getRowsHeight(headers, content);

        int width = computeTotalWidth(columnsWidth);
        int height = computeTotalHeight(rowsHeight, headers != null);

        StringBuilder sb = new StringBuilder(width * height);

        int heightOffset = 0;
        if (headers != null) {
            draw(sb, headers, columnsWidth, rowsHeight[0]);

            sb.append("-".repeat(width)).append('\n');

            heightOffset = 1;
        }

        for (int y = 0; y < tableHeight; y++) {
            draw(sb, content[y], columnsWidth, rowsHeight[y + heightOffset]);
        }

        return sb.toString();
    }

    private static void draw(StringBuilder sb, Cell[] cells, int[] columnsWidth, int cellHeight) {

        for (int y = 0; y < cellHeight; y++) {
            for (int x = 0; x < cells.length; x++) {
                sb.append(' ');
                int w = columnsWidth[x];

                int n = cells[x].draw(sb, w, cellHeight, y);

                if (x + 1 < cells.length) {
                    sb.append(" ".repeat(w - n + 1));
                    sb.append('|');
                }
            }

            sb.append('\n');
        }
    }


    private static int[] getColumnsWidth(Cell[] headers, Cell[][] content, int width) {
        int[] columnsWidth = new int[width];

        for (int x = 0; x < width; x++) {
            if (headers != null) {
                columnsWidth[x] = headers[x].width();
            }

            for (Cell[] cells : content) {
                columnsWidth[x] = Math.max(columnsWidth[x], cells[x].width());
            }
        }

        return columnsWidth;
    }

    private static int[] getRowsHeight(Cell[] headers, Cell[][] content) {
        int offset = 0;

        int[] rowHeights;
        if (headers != null) {
            offset = 1;

            rowHeights = new int[content.length + 1];
            rowHeights[0] = maxHeight(headers);
        } else {
            rowHeights = new int[content.length];
        }

        for (int y = offset; y < rowHeights.length; y++) {
            rowHeights[y] = maxHeight(content[y - offset]);
        }

        return rowHeights;
    }

    private static int maxHeight(Cell[] cells) {
        int max = 0;

        for (Cell c : cells) {
            max = Math.max(max, c.height());
        }

        return max;
    }

    private static int computeTotalWidth(int[] columnsWidth) {
        int width = 2;

        for (int i = 0; i < columnsWidth.length; i++) {
            int w = columnsWidth[i];

            width += w;

            if (i + 1 < columnsWidth.length) {
                width += 3;
            }
        }

        return width;
    }

    private static int computeTotalHeight(int[] rowsHeight, boolean hasHeader) {
        int height = 2 + (hasHeader ? 3 : 0);

        for (int i = 0; i < rowsHeight.length; i++) {
            height += rowsHeight[i];
        }

        return height;
    }
}
