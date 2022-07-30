package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.interval.Interval;
import fr.valax.interval.ParseException;
import fr.valax.interval.Set;
import fr.valax.interval.SetParser;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author PoulpoGaz
 */
public class PrintCommand extends AbstractCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name", allowDuplicate = true)
    protected String[] name;

    @Option(names = {"l", "levels"}, hasArgument = true, argName = "Levels")
    protected String levels;

    private final SetParser parser = new SetParser();

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        List<Pack> packs = getPackMultiple(name);

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

            List<Level> levels = getLevelMultiple(pack, range); // TODO: add methods that returns an iterator

            for (Level l : levels) {
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
