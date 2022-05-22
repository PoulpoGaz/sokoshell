package fr.valax.args.api;

import fr.valax.args.CommandSpecification;
import fr.valax.args.utils.Node;
import fr.valax.args.utils.ParseException;

/**
 * Produce help string when the
 * user doesn't properly use a command
 */
public interface HelpFormatter {

    String commandHelp(ParseException error,
                       CommandSpecification spec);

    String unrecognizedCommand(CommandSpecification parent,
                               Node<CommandSpecification> commands,
                               String[] args);
}
