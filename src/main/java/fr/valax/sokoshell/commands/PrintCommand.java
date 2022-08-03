package fr.valax.sokoshell.commands;

import fr.valax.args.api.Option;
import fr.valax.interval.Interval;
import fr.valax.interval.ParseException;
import fr.valax.interval.Set;
import fr.valax.interval.SetParser;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author PoulpoGaz
 */
public class PrintCommand extends AbstractCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name", allowDuplicate = true)
    protected String[] name;

    @Option(names = {"l", "levels"}, hasArgument = true, argName = "Levels")
    protected String levels;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Collection<Pack> packs = getPackMultiple(name);

        Set range;
        if (levels != null) {
            try {
                range = parser.parse(levels);
            } catch (ParseException e) {
                e.printStackTrace(err);
                return FAILURE;
            }
        } else {
            range = Interval.all();
        }

        for (Pack pack : packs) {
            out.printf("Pack: %s%n", pack.name());

            Iterator<Level> levels = getLevelMultipleIt(pack, range);

            while (levels.hasNext()) {
                Level l = levels.next();

                out.printf("<===== Level n°%d =====>%n", l.getIndex() + 1);
                helper.getRenderer().print(out, l);
            }
        }

        return SUCCESS;
    }

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public String getShortDescription() {
        return "print a state";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
