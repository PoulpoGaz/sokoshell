package fr.valax.args.api;

import fr.valax.args.utils.Node;
import fr.valax.args.utils.ParseException;

/**
 * Produce help string when the
 * user doesn't properly use a command
 * @author PoulpoGaz
 */
public interface HelpFormatter {

    String commandHelp(ParseException error,
                       CommandDescriber command);

    String generalHelp(Node<CommandDescriber> commands,
                       String[] args,
                       boolean unrecognizedCommand);
}
