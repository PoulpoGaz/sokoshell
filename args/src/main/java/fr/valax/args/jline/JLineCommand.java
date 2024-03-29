package fr.valax.args.jline;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Command;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.util.List;

public interface JLineCommand extends Command {

    /**
     * if option is null, the command may complete vaArgs
     */
    default void complete(LineReader reader,
                          String commandString,
                          CommandLine.CommandSpec command,
                          List<Candidate> candidates,
                          CommandLine.OptionSpec option,
                          String argument) {

    }

    /**
     * JLine add by default a help option.
     * When a user use this option with a command (e.g: help load),
     * JLine execute this command: load --help. So, it's preferred to
     * add by default the help option
     */
    @Override
    default boolean addHelp() {
        return true;
    }
}
