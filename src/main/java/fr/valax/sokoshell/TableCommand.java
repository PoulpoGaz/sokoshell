package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;

import java.io.PrintStream;

public abstract class TableCommand extends AbstractCommand {

    @Option(names = {"c", "column"}, hasArgument = true)
    private String column;

    @Option(names = {"I", "column-index"}, hasArgument = true)
    private Integer index;

    @Option(names = {"r", "reversed"})
    private boolean reversed;

    protected void printTable(PrintStream out, PrintStream err, PrettyTable table) {
        if (column == null && index == null) {
            out.println(table.create());
        } else {
            int i;
            if (index != null) {
                i = index - 1;
            } else {
                for (i = 0; i < table.nuberOfColumn(); i++) {
                    PrettyColumn<?> column1 = table.getColumn(i);

                    if (column1.getHeaderStr().equalsIgnoreCase(column)) {
                        break;
                    }
                }
            }

            if (i < 0 || i >= table.nuberOfColumn()) {
                if (index != null) {
                    err.printf("Index out of bounds%n");
                } else {
                    err.printf("No column named %s%n", column);
                }
                return;
            }

            out.println(table.create(i, reversed));
        }
    }
}
