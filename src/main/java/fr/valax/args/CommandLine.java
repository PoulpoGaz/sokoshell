package fr.valax.args;

import fr.valax.args.utils.Node;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.valax.args.utils.ArgsUtils.thrParseExc;

public class CommandLine {

    private static final OptionSpecification DEFAULT_HELP = new OptionSpecificationBuilder()
            .name("h").name("-h")
            .desc("Print this message and exit")
            .build();

    /**
     * Contains all commands in a tree
     * Only the root node is allowed to have null value
     */
    private final Node<CommandSpecification> root;

    /** A class with type T must be associated with a type converted of the same type */
    private final Map<Class<?>, TypeConverter<?>> converters;

    private OptionSpecification help = DEFAULT_HELP;

    CommandLine(Node<CommandSpecification> root) {
        this.root = root;
        converters = new HashMap<>();
    }

    public CommandLine addDefaultConverters() {
        converters.put(String.class, TypeConverters.STRING);
        converters.put(Byte.class, TypeConverters.BYTE);
        converters.put(Short.class, TypeConverters.SHORT);
        converters.put(Integer.class, TypeConverters.INT);
        converters.put(Long.class, TypeConverters.LONG);
        converters.put(Float.class, TypeConverters.FLOAT);
        converters.put(Double.class, TypeConverters.DOUBLE);
        converters.put(Path.class, TypeConverters.PATH);

        return this;
    }

    public Object parse(String[] args) throws CommandLineException {
        return parseSub(root, args, 0);
    }

    protected Object parseSub(Node<CommandSpecification> parent, String[] args, int index) throws CommandLineException {
        if (index >= args.length) {
            CommandSpecification spec = parent.getValue();

            if (spec == null) { // for root
                thrParseExc("Unrecognized command: %s", Arrays.toString(args));
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
                thrParseExc("Unrecognized command: %s", Arrays.toString(args));
            }
        }

        return null;
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
                if (spec.getCommand().help()) {
                    //HelpFormatter.printHelp(spec.getAction());
                    return null;
                } else {
                    //throw new CommandLineException("In " + spec.getName(), e);
                    throw e;
                }
            }

            spec.setOptions();

            return spec.getCommand().execute();


        } finally {
            spec.getOptions().reset();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> getConverter(Class<T> class_) {
        return (TypeConverter<T>) converters.get(class_);
    }

    public OptionSpecification getHelp() {
        return help;
    }

    public void setHelp(OptionSpecification help) {
        this.help = help;
    }
}
