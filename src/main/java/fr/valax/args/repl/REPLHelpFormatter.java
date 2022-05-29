package fr.valax.args.repl;

import fr.valax.args.DefaultHelpFormatter;
import fr.valax.args.api.CommandDescriber;
import fr.valax.args.utils.CommandLineException;
import org.jline.builtins.Options;
import org.jline.builtins.Styles;

public class REPLHelpFormatter extends DefaultHelpFormatter {

    @Override
    public String commandHelp(CommandLineException error, CommandDescriber command) {
        String str = super.commandHelp(error, command);

        return Options.HelpException.highlight(str, Styles.helpStyle()).toAnsi();
    }
}
