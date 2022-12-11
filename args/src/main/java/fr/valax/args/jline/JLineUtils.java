package fr.valax.args.jline;

import fr.valax.args.CommandLine;
import fr.valax.args.api.CommandDescriber;
import fr.valax.args.utils.INode;
import org.jline.reader.EndOfFileException;
import org.jline.reader.impl.completer.SystemCompleter;

import java.io.InputStream;
import java.io.PrintStream;

public class JLineUtils {

    public static SystemCompleter createCompleter(CommandLine cli) {
        SystemCompleter completer = new SystemCompleter();

        for (INode<CommandDescriber> command : cli.getCommands().getChildren()) {
            completer.add(command.getValue().getName(), new ShellCompleter(cli));
        }
        completer.compile();

        return completer;
    }

    public static JLineCommand newExitCommand(String programName) {
        return new JLineCommand() {
            @Override
            public int execute(InputStream in, PrintStream out, PrintStream err) {
                throw new EndOfFileException();
            }

            @Override
            public String getName() {
                return "exit";
            }

            @Override
            public String getShortDescription() {
                return "Exit " + programName;
            }

            @Override
            public String[] getUsage() {
                return new String[] {"exit"};
            }
        };
    }
}
