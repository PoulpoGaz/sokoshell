package fr.valax.args;

import fr.valax.args.api.HelpFormatter;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.Node;
import fr.valax.args.utils.ParseException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static fr.valax.args.utils.ArgsUtils.thrParseExc;

/**
 * @author PoulpoGaz
 */
public class CommandLine {

    /**
     * Contains all commands in a tree
     * Only the root node is allowed to have null value
     */
    private final Node<CommandSpecification> root;

    /** A class with type T must be associated with a type converted of the same type */
    private final Map<Class<?>, TypeConverter<?>> converters;

    private final HelpFormatter helpFormatter;
    private boolean showHelp = true;

    CommandLine(Node<CommandSpecification> root,
                Map<Class<?>, TypeConverter<?>> converters,
                HelpFormatter helpFormatter) {
        this.root = root;
        this.converters = converters;
        this.helpFormatter = helpFormatter;
    }

    public Object execute(String[] args) throws CommandLineException {
        ParseCommand parseCommand = getCommand(root, args, 0);

        CommandSpecification spec = parseCommand.node().getValue();

        if (parseCommand.unrecognized()) {
            unrecognizedCommand(spec, args);
            return null;
        } else {
            return executeCommand(spec, args, parseCommand.index(), args.length);
        }
    }

    private ParseCommand getCommand(Node<CommandSpecification> node, String[] command, int index) {
        if (index >= command.length) {
            CommandSpecification spec = node.getValue();

            if (spec == null) { // for root
                return new ParseCommand(node, true, index);
            } else {
                return new ParseCommand(node, false, index);
            }

        } else {
            Node<CommandSpecification> next = null;
            for (Node<CommandSpecification> child : node.getChildren()) {
                CommandSpecification spec = child.getValue();
                if (command[index].equals(spec.getName())) {
                    next = child;
                    break;
                }
            }

            if (next != null) {
                return getCommand(next, command, index + 1);
            } else if (node.getValue() != null) {
                return new ParseCommand(node, false, index);
            } else {
                return new ParseCommand(node, true, index);
            }
        }
    }

    private void unrecognizedCommand(CommandSpecification last, String[] args) throws ParseException {
        if (showHelp()) {
            System.out.println(helpFormatter.generalHelp(last, root, args, true));
        } else {
            thrParseExc("Unrecognized command: %s", Arrays.toString(args));
        }
    }

    protected Object executeCommand(CommandSpecification spec, String[] args, int start, int end)
            throws CommandLineException {
        try {


            try {
                if (spec.getVaargs() != null) {
                    List<String> vaArgs = spec.getOptions().parseAllowVaArgs(args, start, end);

                    spec.setVaArgs(vaArgs);
                } else {
                    spec.getOptions().parse(args, start, end);
                }
            } catch (ParseException e) {
                if (showHelp) {
                    System.out.println(helpFormatter.commandHelp(e, spec));
                    return null;
                } else {
                    throw e;
                }
            }

            if (spec.getCommand().addHelp() && spec.getHelp().isPresent()) {
                System.out.println(helpFormatter.commandHelp(null, spec));

                return null;
            } else {
                spec.setOptions();

                return spec.getCommand().execute();
            }
        } finally {
            spec.getOptions().reset();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> getConverter(Class<T> class_) {
        return (TypeConverter<T>) converters.get(class_);
    }

    public String getGeneralHelp() {
        return helpFormatter.generalHelp(null, root, null, false);
    }

    public String getCommandHelp(String command) {
        Node<CommandSpecification> spec;

        if (command.isBlank()) {
            spec = root;
        } else {
            String[] split = command.split(" ");

            spec = getCommandHelp(root, split, 0);
        }

        if (spec != null && spec.getValue() != null) {
            return helpFormatter.commandHelp(null, spec.getValue());
        } else {
            return "Unknown command: " + command;
        }
    }

    private Node<CommandSpecification> getCommandHelp(Node<CommandSpecification> node, String[] parts, int index) {
        if (index < parts.length) {
            for (Node<CommandSpecification> child : node.getChildren()) {

                CommandSpecification spec = child.getValue();
                if (parts[index].equals(spec.getName())) {
                    return getCommandHelp(child, parts, index + 1);
                }
            }

            return null;
        } else {
            return node;
        }
    }

    public boolean showHelp() {
        return showHelp;
    }

    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
    }

    Node<CommandSpecification> getRoot() {
        return root;
    }

    CommandSpecification getCommand(String[] command) {
        ParseCommand c = getCommand(root, command, 0);

        if (c.unrecognized()) {
            return null;
        } else {
            return c.node().getValue();
        }
    }

    CommandSpecification getCommand(String command) {
        return getCommand(ArgsUtils.splitQuoted(command));
    }

    private record ParseCommand(Node<CommandSpecification> node, boolean unrecognized, int index) {}
}
