package fr.valax.sokoshell;

import fr.valax.sokoshell.utils.PrettyTable;

import java.io.PrintStream;
import java.util.List;

public abstract class TableCommand<T> extends AbstractCommand {

    protected void printTable(PrintStream out,
                              List<T> elements) {
        if (elements.isEmpty()) {
            out.println(whenEmpty());
        } else {
            String[] headers = getHeaders();

            String table = PrettyTable.create(
                    headers.length, elements.size(), headers,
                    (x, y) -> extract(elements.get(y), x));

            out.println(table);

            String line = countLine();
            if (line != null) {
                out.println();
                out.printf(line, elements.size());
            }
        }
    }

    protected abstract String[] getHeaders();

    protected abstract PrettyTable.Cell extract(T t, int x);

    protected abstract String countLine();

    protected abstract String whenEmpty();
}
