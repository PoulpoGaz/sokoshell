package fr.valax.args.api;

import fr.valax.args.CommandSpecification;
import fr.valax.args.utils.Node;
import fr.valax.args.utils.ParseException;

/**
 * Produce help string when the
 * user doesn't properly use a command
 * @author PoulpoGaz
 */
public interface HelpFormatter {

    String commandHelp(ParseException error,
                       CommandSpecification spec);

    String generalHelp(CommandSpecification parent,
                       Node<CommandSpecification> commands,
                       String[] args,
                       boolean unrecognizedCommand);
}
