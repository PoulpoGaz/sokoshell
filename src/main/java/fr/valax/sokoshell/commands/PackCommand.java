package fr.valax.sokoshell.commands;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.util.List;

public abstract class PackCommand extends AbstractCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name")
    protected String name;

    @Override
    public void completeOption(LineReader reader, String argument, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }
}
