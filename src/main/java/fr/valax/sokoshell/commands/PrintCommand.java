package fr.valax.sokoshell.commands;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.interval.Set;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author PoulpoGaz
 */
public class PrintCommand extends AbstractCommand {

    @Option(names = {"p", "packs"}, hasArgument = true, argName = "Pack name", allowDuplicate = true)
    protected String[] packs;

    @Option(names = {"l", "levels"}, hasArgument = true, argName = "Levels")
    protected String levels;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Collection<Pack> packs = getPacks(this.packs);

        if (packs.isEmpty()) {
            return SUCCESS;
        }

        Set range = createSet(this.levels);

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

    @Override
    public void completeOption(LineReader reader, String argument, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }
}
