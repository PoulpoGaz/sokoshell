package fr.valax.args;

import fr.valax.args.api.HelpFormatter;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.Node;
import fr.valax.args.utils.ParseException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static fr.valax.args.utils.ArgsUtils.thrParseExc;

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

    public Object parse(String[] args) throws CommandLineException {
        return parseSub(root, args, 0);
    }

    protected Object parseSub(Node<CommandSpecification> parent, String[] args, int index) throws CommandLineException {
        if (index >= args.length) {
            CommandSpecification spec = parent.getValue();

            if (spec == null) { // for root
                unrecognizedCommand(null, args);
            } else {
                return execute(spec, args, args.length, args.length);
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
                parseSub(next, args, index + 1);
            } else if (parent.getValue() != null) {
                return execute(parent.getValue(), args, index, args.length);
            } else {
                unrecognizedCommand(parent.getValue(), args);
            }
        }

        return null;
    }

    private void unrecognizedCommand(CommandSpecification last, String[] args) throws ParseException {
        if (showHelp()) {
            System.out.println(helpFormatter.unrecognizedCommand(last, root, args));
        } else {
            thrParseExc("Unrecognized command: %s", Arrays.toString(args));
        }
    }

    protected Object execute(CommandSpecification spec, String[] args, int start, int end)
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

    public boolean showHelp() {
        return showHelp;
    }

    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
    }
}
