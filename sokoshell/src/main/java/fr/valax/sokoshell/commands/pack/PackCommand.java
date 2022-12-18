package fr.valax.sokoshell.commands.pack;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.commands.AbstractCommand;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.util.List;

public abstract class PackCommand extends AbstractCommand {

    @Option(names = {"p", "pack"}, hasArgument = true, argName = "Pack name")
    protected String pack;

    @Override
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option != null && ArgsUtils.contains(option.getShortNames(), 'p')) {
            helper.addPackCandidates(candidates);
        }
    }
}
