package fr.valax.sokoshell.utils;

import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class PrettyColumn<T> {

    private PrettyCell.Header header;
    private List<PrettyCell<T>> content;
    private Comparator<T> comparator;
    private Function<T, AttributedString[]> toString;

    public PrettyColumn(String header) {
        this(new AttributedString[] {new AttributedString(header)});
    }

    public PrettyColumn(AttributedString header) {
        this(new AttributedString[] {header});
    }

    public PrettyColumn(AttributedString[] header) {
        this.header = new PrettyCell.Header(-1, header);
        content = new ArrayList<>();
    }

    public int width(boolean header) {
        int width = header ? this.header.width() : 0;

        for (PrettyCell<T> cell : content) {
            width = Math.max(cell.width(), width);
        }

        return width;
    }

    public void add(T t) {
        content.add(new PrettyCell<>(content.size(), this, t));
    }

    public void add(Alignment xAlignment, T t) {
        content.add(new PrettyCell<>(content.size(), this, t, xAlignment, Alignment.CENTER));
    }

    public void add(Alignment xAlignment, Alignment yAlignment, T t) {
        content.add(new PrettyCell<>(content.size(), this, t, xAlignment, yAlignment));
    }

    public void remove(int index) {
        content.remove(index);

        for (; index < content.size(); index++) {
            content.get(index).y--;
        }
    }

    public List<PrettyCell<T>> getContent() {
        return content;
    }

    public int size() {
        return content.size();
    }

    public Comparator<T> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public Function<T, AttributedString[]> getToString() {
        return toString;
    }

    public void setToString(Function<T, AttributedString[]> toString) {
        this.toString = toString;
    }

    protected PrettyCell.Header getHeader() {
        return header;
    }

    public String getHeaderStr() {
        return header.content[0].toAnsi();
    }
}
