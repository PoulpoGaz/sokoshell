package fr.valax.args.repl;

import fr.valax.args.api.Command;
import org.jline.reader.Completer;

public interface REPLCommand<T> extends Command<T>, Completer {

}
