package fr.valax.args.repl;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public interface REPLCommand<T> extends Command<T> {

    void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option);
}
