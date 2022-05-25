package fr.valax.args;

import fr.valax.args.api.HelpFormatter;
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
        return executeSub(root, args, 0);
    }

    protected Object executeSub(Node<CommandSpecification> parent, String[] args, int index) throws CommandLineException {
        if (index >= args.length) {
            CommandSpecification spec = parent.getValue();

            if (spec == null) { // for root
                unrecognizedCommand(null, args);
            } else {
                return executeCommand(spec, args, args.length, args.length);
            }

        } else {
            Node<CommandSpecification> next = null;
            for (Node<CommandSpecification> child : parent.getChildren()) {
                CommandSpecification spec = child.getValue();
                if (args[index].equals(spec.getName())) {
                    next = child;
                    break;
                }
            }

            if (next != null) {
                return executeSub(next, args, index + 1);
            } else if (parent.getValue() != null) {
                return executeCommand(parent.getValue(), args, index, args.length);
            } else {
                unrecognizedCommand(parent.getValue(), args);
            }
        }

        return null;
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
}
