package fr.valax.sokoshell.utils;

import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PrettyTable {

    public static final AttributedString[] EMPTY = wrap("");

    public static AttributedString[] wrap(Object object) {
        return wrap(Objects.toString(object));
    }

    public static AttributedString[] wrap(CharSequence str) {
        return new AttributedString[] {new AttributedString(str)};
    }


    public static final Skin DEFAULT = new Skin("│", "─", "┼");

    private Skin skin = DEFAULT;
    private final List<PrettyColumn<?>> columns;

    private boolean showHeader = true;

    public PrettyTable() {
        columns = new ArrayList<>();
    }

    public String create() {
        return create(-1, false);
    }

    public String create(int columnToSort, boolean reverse) {
        if (columns.isEmpty()) {
            return "";
        }

        int tableHeight = getTableHeight();
        int[] columnsWidth = getColumnsWidth();
        int[] rowsHeight = getRowsHeight(tableHeight);

        int width = computeTotalWidth(columnsWidth);
        int height = computeTotalHeight(rowsHeight);

        StringBuilder sb = new StringBuilder(width * height);

        int heightOffset = 0;
        if (showHeader) {
            draw(skin, sb, (x) -> columns.get(x).getHeader(), columnsWidth, rowsHeight[0]);

            for (int i = 0; i < columnsWidth.length; i++) {
                int w = columnsWidth[i];
                sb.append(skin.headerDelimiter().repeat(w + 2));

                if (i + 1 < columnsWidth.length) {
                    sb.append(skin.intersection());
                }
            }

            sb.append('\n');

            heightOffset = 1;
        }

        if (columnToSort < 0 || columnToSort >= columns.size()) {
            for (int y = 0; y < tableHeight; y++) {
                int finalY = y;
                draw(skin, sb, (x) -> get(x, finalY), columnsWidth, rowsHeight[y + heightOffset]);
            }
        } else {
            drawSorted(columns.get(columnToSort), reverse, sb, columnsWidth, rowsHeight, heightOffset);
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> void drawSorted(PrettyColumn<T> column, boolean reversed,
                                final StringBuilder sb, int[] columnsWidth, int[] cellHeight, int offset) {
        List<PrettyCell<T>> cells = column.getContent();

        Comparator<T> comp = column.getComparator();
        if (comp == null) {
            comp = (Comparator<T>) Comparator.naturalOrder();
        }
        if (reversed) {
            comp = comp.reversed();
        }

        cells.stream()
                .sorted(Comparator.comparing(PrettyCell::getContent, comp))
                .forEach((c) -> {
                    int y = c.getY();
                    draw(skin, sb, (x) -> get(x, y), columnsWidth, cellHeight[y + offset]);
                });
    }

    private void draw(Skin skin, StringBuilder sb, Function<Integer, PrettyCell<?>> getter, int[] columnsWidth, int cellHeight) {

        for (int y = 0; y < cellHeight; y++) {
            for (int x = 0; x < columns.size(); x++) {
                sb.append(' ');
                int w = columnsWidth[x];

                PrettyCell<?> cell = getter.apply(x);

                int n = 0;
                if (cell != null) {
                    n = cell.draw(sb, w, cellHeight, y);
                }

                if (x + 1 < columns.size()) {
                    sb.append(" ".repeat(w - n + 1));
                    sb.append(skin.columnDelimiter());
                }
            }

            sb.append('\n');
        }
    }

    private int getTableHeight() {
        int height = 0;

        for (PrettyColumn<?> c : columns) {
            height = Math.max(height, c.size());
        }

        return height;
    }

    private int[] getColumnsWidth() {
        int[] columnsWidth = new int[columns.size()];

        for (int x = 0; x < columnsWidth.length; x++) {
            PrettyColumn<?> column = columns.get(x);

            columnsWidth[x] = column.width(showHeader);
        }

        return columnsWidth;
    }

    private int[] getRowsHeight(int height) {
        int[] rowHeights = new int[height + (showHeader ? 1 : 0)];

        int offset = 0;
        if (showHeader) {
            offset = 1;
            rowHeights[0] = 1;
        }

        for (int y = 0; y < height; y++) {
            int h = 0;

            for (int x = 0; x < columns.size(); x++) {
                PrettyCell<?> cell = get(x, y);

                if (cell != null) {
                    h = Math.max(cell.height(), h);
                }
            }

            rowHeights[y + offset] = h;
        }

        return rowHeights;
    }

    private int computeTotalWidth(int[] columnsWidth) {
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

    private int computeTotalHeight(int[] rowsHeight) {
        int height = 2 + (showHeader ? 3 : 0);

        for (int j : rowsHeight) {
            height += j;
        }

        return height;
    }

    private PrettyCell<?> get(int x, int y) {
        if (x < 0 || x >= columns.size() || y < 0) {
            return null;
        }

        PrettyColumn<?> column = columns.get(x);
        if (y >= column.size()) {
            return null;
        }

        return column.getContent().get(y);
    }
    
    public void addColumn(PrettyColumn<?> column) {
        columns.add(Objects.requireNonNull(column));
    }

    public void removeColumn(int index) {
        columns.remove(index);
    }

    public PrettyColumn<?> getColumn(int index) {
        return columns.get(index);
    }

    public int nuberOfColumn() {
        return columns.size();
    }

    public void setSkin(Skin skin) {
        this.skin = Objects.requireNonNull(skin);
    }

    public Skin getSkin() {
        return skin;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }

    public record Skin(String columnDelimiter, String headerDelimiter, String intersection) {

        public Skin(String columnDelimiter, String headerDelimiter, String intersection) {
            this.columnDelimiter = columnDelimiter;
            this.headerDelimiter = headerDelimiter;
            this.intersection = intersection;

            if (columnDelimiter.length() != 1 || headerDelimiter.length() != 1 || intersection.length() != 1) {
                throw new IllegalArgumentException();
            }
        }
    }
}
