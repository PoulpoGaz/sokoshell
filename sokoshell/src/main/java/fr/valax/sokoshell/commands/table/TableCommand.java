package fr.valax.sokoshell.commands.table;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.commands.AbstractCommand;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;

import java.io.PrintStream;

public abstract class TableCommand extends AbstractCommand {

    @Option(names = {"c", "column"}, hasArgument = true)
    protected String column;

    @Option(names = {"I", "column-index"}, hasArgument = true)
    protected Integer index;

    @Option(names = {"r", "reversed"})
    protected boolean reversed;

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
