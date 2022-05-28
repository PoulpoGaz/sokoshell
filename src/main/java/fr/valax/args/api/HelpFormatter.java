package fr.valax.args.api;

import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.INode;
import fr.valax.args.utils.ParseException;

/**
 * Produce help string when the
 * user doesn't properly use a command
 * @author PoulpoGaz
 */
public interface HelpFormatter {

    String commandHelp(CommandLineException error,
                       CommandDescriber command);

    String generalHelp(INode<CommandDescriber> commands,
                       String[] args,
                       boolean unrecognizedCommand);
}
