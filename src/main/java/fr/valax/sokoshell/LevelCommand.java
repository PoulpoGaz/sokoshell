package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public abstract class LevelCommand extends AbstractVoidCommand {

    @Option(names = {"p", "-pack"}, hasArgument = true, argName = "Pack name", optional = false)
    protected String name;

    @Option(names = {"i", "-index"}, hasArgument = true, argName = "Level index", optional = false)
    protected int index;

    protected Level getLevel() {
        Pack pack = helper.getPack(name);

        if (pack == null) {
            System.out.printf("No pack named %s exists%n", name);
            return null;
        }

        index--;
        if (index < 0 || index >= pack.levels().size()) {
            System.out.println("Index out of bounds");
            return null;
        }

        return pack.levels().get(index);
    }

    @Override
    public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }
}
