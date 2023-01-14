package fr.valax.sokoshell.commands.table;

import fr.valax.sokoshell.graphics.style.MapStyle;
import fr.valax.sokoshell.utils.PrettyColumn;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.utils.AttributedString;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;

import static org.jline.utils.AttributedStyle.BOLD;

public class ListStyle extends TableCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        MapStyle selected = sokoshell().getMapStyle();

        List<MapStyle> mapStyles = sokoshell().getMapStyles().stream()
                .sorted(Comparator.comparing(MapStyle::getName))
                .toList();

        PrettyTable table = new PrettyTable();

        PrettyColumn<AttributedString> name = new PrettyColumn<>("name");
        name.setToString((s) -> new AttributedString[] {s});
        name.setComparator(Utils.ATTRIBUTED_STRING_COMPARATOR);

        PrettyColumn<String> author = new PrettyColumn<>("author");
        PrettyColumn<String> version = new PrettyColumn<>("version");

        for (MapStyle style : mapStyles) {
            if (selected == style) {
                name.add(new AttributedString("* " +style.getName() + " *" , BOLD));
            } else {
                name.add(new AttributedString(style.getName()));
            }

            author.add(style.getAuthor());
            version.add(style.getVersion());
        }

        table.addColumn(name);
        table.addColumn(author);
        table.addColumn(version);

        printTable(out, err, table);

        return 0;
    }

    @Override
    public String getName() {
        return "style";
    }

    @Override
    public String getShortDescription() {
        return "List all styles";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}