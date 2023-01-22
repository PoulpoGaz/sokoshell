package fr.valax.sokoshell.commands.table;

import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.Alignment;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;

public class ListPacks extends TableCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        List<Pack> packs = sokoshell().getPacks().stream()
                .sorted(Comparator.comparing(Pack::name))
                .toList();

        PrettyTable table = new PrettyTable();

        PrettyColumn<String> name = new PrettyColumn<>("Name");
        PrettyColumn<String> author = new PrettyColumn<>("Author");
        PrettyColumn<Integer> version = new PrettyColumn<>("Number of levels");

        for (Pack pack : packs) {
            name.add(pack.name());
            author.add(pack.author());
            version.add(Alignment.RIGHT, pack.nLevels());
        }

        table.addColumn(name);
        table.addColumn(author);
        table.addColumn(version);

        printTable(out, err, table);

        int totalLevels = 0;
        for (Pack p : packs) {
            totalLevels += p.nLevels();
        }
        out.printf("%nTotal packs: %d - Total levels: %d%n", packs.size(), totalLevels);

        return 0;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getShortDescription() {
        return "List all packs";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
