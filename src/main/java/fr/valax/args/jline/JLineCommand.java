package fr.valax.args.jline;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public interface JLineCommand extends Command {

    default void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {

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
