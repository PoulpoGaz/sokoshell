package fr.valax.args;

import fr.valax.args.api.*;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.Node;
import fr.valax.args.utils.ParseException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static fr.valax.args.utils.ArgsUtils.*;
import static fr.valax.args.utils.ArgsUtils.thrExc;

/**
 * @author PoulpoGaz
 */
public class CommandLine {

    /**
     * Contains all commands in a tree
     * Only the root node is allowed to have null value
     */
    private final Node<CommandSpec> root;

    /** A class with type T must be associated with a type converted of the same type */
    private final Map<Class<?>, TypeConverter<?>> converters;

    private final HelpFormatter helpFormatter;
    private boolean showHelp = true;

    CommandLine(Node<CommandSpec> root,
                Map<Class<?>, TypeConverter<?>> converters,
                HelpFormatter helpFormatter) {
        this.root = root;
        this.converters = converters;
        this.helpFormatter = helpFormatter;
    }

    public Object execute(String[] args) throws CommandLineException {
        ParseCommand parseCommand = getCommand(root, args, 0);

        CommandSpec spec = parseCommand.node().getValue();

        if (parseCommand.unrecognized()) {
            unrecognizedCommand(args);
            return null;
        } else {
            return executeCommand(spec, args, parseCommand.index(), args.length);
        }
    }

    private ParseCommand getCommand(Node<CommandSpec> node, String[] command, int index) {
        if (index >= command.length) {
            CommandSpec spec = node.getValue();

            if (spec == null) { // for root
                return new ParseCommand(node, true, index);
            } else {
                return new ParseCommand(node, false, index);
            }

        } else {
            Node<CommandSpec> next = null;
            for (Node<CommandSpec> child : node.getChildren()) {
                CommandSpec spec = child.getValue();
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

    private void unrecognizedCommand(String[] args) throws ParseException {
        if (showHelp()) {
            //System.out.println(helpFormatter.generalHelp(last, root, args, true));
        } else {
            thrParseExc("Unrecognized command: %s", Arrays.toString(args));
        }
    }

    protected Object executeCommand(CommandSpec spec, String[] args, int start, int end)
            throws CommandLineException {
        try {


            try {
                if (spec.getVaargs() != null) {
                    List<String> vaArgs = spec.parseAllowVaArgs(args, start, end);

                    spec.setVaArgs(vaArgs);
                } else {
                    spec.parse(args, start, end);
                }
            } catch (ParseException e) {
                if (showHelp) {
                    //System.out.println(helpFormatter.commandHelp(e, spec));
                    return null;
                } else {
                    throw e;
                }
            }

            if (spec.getCommand().addHelp() && spec.getHelp().isPresent()) {
                //System.out.println(helpFormatter.commandHelp(null, spec));

                return null;
            } else {
                spec.setOptions();

                return spec.getCommand().execute();
            }
        } finally {
            spec.reset();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> getConverter(Class<T> class_) {
        return (TypeConverter<T>) converters.get(class_);
    }

    public String getGeneralHelp() {
        return null;//helpFormatter.generalHelp(null, root, null, false);
    }

    public String getCommandHelp(String command) {
        Node<CommandSpec> spec;

        if (command.isBlank()) {
            spec = root;
        } else {
            String[] split = command.split(" ");

            spec = getCommandHelp(root, split, 0);
        }

        if (spec != null && spec.getValue() != null) {
            return null; //helpFormatter.commandHelp(null, spec.getValue());
        } else {
            return "Unknown command: " + command;
        }
    }

    private Node<CommandSpec> getCommandHelp(Node<CommandSpec> node, String[] parts, int index) {
        if (index < parts.length) {
            for (Node<CommandSpec> child : node.getChildren()) {

                CommandSpec spec = child.getValue();
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

    private record ParseCommand(Node<CommandSpec> node, boolean unrecognized, int index) {}


    // ===============
    // * CommandSpec *
    // ===============

    /**
     * An object used to describe a command and for setting fields.
     * It is divided in three parts:
     *  - initialization: create OptionSpec and CommandDescriber
     *  - parsing: parse the options
     *  - reflection: set fields in the command
     */
    private class CommandSpec {

        private final Command<?> command;
        private final CommandDescriber describer;

        private final Map<String, OptionSpec> options;
        private final OptionSpec help;
        private Field vaargs;

        public CommandSpec(Command<?> command) throws CommandLineException {
            Objects.requireNonNull(command.getName());
            this.command = command;

            OptionsBuilder builder = new OptionsBuilder();

            createOptions(builder);
            if (command.addHelp()) {
                help = new OptionSpec(
                        new String[]{"-h", "--help"},
                        true, false, false,
                        null, "Print help for this command", null, null);

                if (builder.addOption(help) != null) {
                    thrExc("Two option have same name");
                }
            } else {
                help = null;
            }

            this.options = builder.getOptions();
            this.describer = new CommandDescriberImpl(this, builder.getOptionsByGroup());
        }

        private void createOptions(OptionsBuilder builder) throws CommandLineException {
            Class<?> class_ = command.getClass();

            for (Field field : getAllFields(class_)) {
                OptionGroup optGroup = null;
                if (field.isAnnotationPresent(OptionGroup.class)) {
                    optGroup = field.getAnnotation(OptionGroup.class);
                }

                if (field.isAnnotationPresent(Option.class)) {
                    checkNotFinal(field);
                    field.setAccessible(true);

                    Option option = field.getAnnotation(Option.class);

                    if (option.allowDuplicate()) {
                        checkArray(field);
                    } else {
                        checkNotArray(field);
                    }

                    if (!option.hasArgument()) {
                        checkBoolean(field, "%s should be a boolean", field.getName());
                    }

                    OptionSpec spec = new OptionSpec(option, field);
                    OptionSpec old = builder.addOption(optGroup, spec);

                    if (old != null) {
                        thrExc("Two option have same name");
                    }

                } else if (field.isAnnotationPresent(VaArgs.class)) {
                    field.setAccessible(true);
                    if (vaargs != null) {
                        thrExc("Command can't have two 'VaArgs' (%s)", getName());
                    }
                    checkNotFinal(field);
                    checkArray(field);

                    vaargs = field;
                }
            }
        }

        private List<Field> getAllFields(Class<?> class_) {
            List<Field> fields = new ArrayList<>();

            Class<?> target = class_;
            while (!target.equals(Object.class)) {
                fields.addAll(List.of(target.getDeclaredFields()));

                target = target.getSuperclass();
            }

            return fields;
        }

        // * PARSING * //

        public void parse(String[] args, int start, int end) throws ParseException {
            List<String> str = parseAllowVaArgs(args, start, end);

            if (str.size() > 0) {
                throw new ParseException("Unrecognized parameters: " + str);
            }
        }

        public List<String> parseAllowVaArgs(String[] args, int start, int end) throws ParseException {
            List<String> vaargs = new ArrayList<>();
            for (int i = start; i < end; i++) {
                String arg = args[i];

                OptionSpec option;
                if (arg.startsWith("-")) {
                    option = options.get(arg.substring(1));

                    if (option == null) {
                        throw new ParseException("Unrecognized option: " + arg);
                    }

                    i = parseOption(option, args, i, end);
                } else {
                    vaargs.add(arg);
                }
            }
            checkNotOptionalOption();

            return vaargs;
        }

        private int parseOption(OptionSpec option, String[] args, int i, int end) throws ParseException {
            String arg = args[i];

            if (option.isPresent() && !option.allowDuplicate()) {
                throw new ParseException("Duplicate parameter: " + option.firstName());
            }

            if (i + 1 < end && !args[i + 1].startsWith("-")) {
                if (option.hasArgument()) {
                    i++;
                    option.addArguments(args[i]);
                } else {
                    throw new ParseException("Option %s doesn't require a parameter".formatted(arg));
                }
            } else if (option.hasArgument()) {
                if (option.getDefaultValue() == null || option.getDefaultValue().isEmpty()) {
                    throw new ParseException("Option %s require a parameter".formatted(arg));
                } else {
                    option.addArguments(option.getDefaultValue());
                }
            }

            option.markPresent();

            return i;
        }

        private void checkNotOptionalOption() throws ParseException {
            for (OptionSpec opt : options.values()) {
                if (!opt.isOptional() && !opt.isPresent()) {
                    throw new ParseException(opt.firstName() + " is required");
                }
            }
        }

        public void reset() {
            for (OptionSpec opt : options.values()) {
                opt.reset();
            }
        }


        // * REFLECTION * //

        public void setOptions() throws CommandLineException {
            for (OptionSpec opt : options.values()) {
                Field field = opt.getField();

                if (opt.hasArgument()) {
                    List<String> args = opt.getArgumentsList();

                    if (opt.allowDuplicate()) {
                        if (opt.isPresent()) {
                            setArray(field, args.toArray(new String[0]));
                        } else {
                            setArray(field, new String[] {opt.getDefaultValue()});
                        }
                    } else {
                        if (opt.isPresent()) {
                            setValue(field, args.get(0));
                        } else {
                            setValue(field, opt.getDefaultValue());
                        }
                    }
                } else {
                    setValue(field, opt.isPresent());
                }
            }
        }

        public void setVaArgs(List<String> vaArgs) throws CommandLineException {
            setArray(vaargs, vaArgs.toArray(new String[0]));
        }

        private TypeConverter<?> getTypeConverterFor(Field field) {
            // TODO: doesn't work
            TypeConverter<?> converter = null;
            if (field.isAnnotationPresent(Option.class)) {
                Option option = field.getAnnotation(Option.class);

                if (option.converter().length > 0) {
                    converter = getConverter(option.converter()[0]);
                }
            } else if (field.isAnnotationPresent(VaArgs.class)) {
                VaArgs vaArgs = field.getAnnotation(VaArgs.class);

                if (vaArgs.converter().length > 0) {
                    converter = getConverter(vaArgs.converter()[0]);
                }
            }

            if (converter == null) {
                Class<?> type = field.getType();
                if (type.isArray()) {
                    return getConverter(type.getComponentType());
                } else {
                    return getConverter(type);
                }
            } else {
                return converter;
            }
        }

        private void setValue(Field field, String value) throws CommandLineException {
            TypeConverter<?> converter = getTypeConverterFor(field);
            notNull(converter, "Can't find converter for %s", vaargs);

            Object o = converter.convert(value);
            setValue(field, o);
        }

        /**
         * Some black magic
         */
        private void setArray(Field field, String[] values) throws CommandLineException {
            Class<?> type = field.getType().getComponentType();
            TypeConverter<?> converter = getTypeConverterFor(field);
            notNull(converter, "Can't find converter for %s", vaargs);

            Object[] array = (Object[]) Array.newInstance(type, values.length);

            for (int i = 0; i < values.length; i++) {
                array[i] = converter.convert(values[i]);
            }

            setValue(field, array);
        }

        private void setValue(Field field, Object value) throws CommandLineException {
            try {
                field.set(command, value);
            } catch (IllegalAccessException e) {
                throw new CommandLineException(e);
            }
        }

        private void checkNotFinal(Field field) throws CommandLineException {
            if (Modifier.isFinal(field.getModifiers())) {
                thrExc("%s is final (in %s)", field.getName(), getName());
            }
        }

        private void checkArray(Field field) throws CommandLineException {
            if (!field.getType().isArray()) {
                thrExc("%s should be an array (in %s)", field.getName(), getName());
            }
        }

        private void checkNotArray(Field field) throws CommandLineException {
            if (field.getType().isArray()) {
                thrExc("%s shouldn't be an array (in %s)", field.getName(), getName());
            }
        }
        /**
         * @param field check if the type of field is boolean or array of boolean
         */
        private void checkBoolean(Field field, String err, Object... args) throws CommandLineException {
            Class<?> type = field.getType();
            if (type.isArray()) {
                type = type.getComponentType();
            }

            if (!type.isAssignableFrom(boolean.class)) {
                thrExc(err, args);
            }
        }

        public String getName() {
            return command.getName();
        }

        public Command<?> getCommand() {
            return command;
        }

        public Field getVaargs() {
            return vaargs;
        }

        public OptionSpec getHelp() {
            return help;
        }

        public Map<String, OptionSpec> getOptions() {
            return options;
        }

        public CommandDescriber getDescriber() {
            return describer;
        }
    }



    // ==================
    // * OptionsBuilder *
    // ==================

    /**
     * Create OptionSpec and organize data
     */
    private static class OptionsBuilder {

        private final Map<OptionGroup, List<Option>> optionsByGroup = new HashMap<>();
        private final Map<String, OptionSpec> options = new HashMap<>();

        public OptionSpec addOption(OptionSpec option) {
            return addOption(null, option);
        }

        public OptionSpec addOption(OptionGroup group, OptionSpec option) {
            OptionSpec old = options.put(option.firstName(), option);

            List<Option> list = optionsByGroup.computeIfAbsent(group, k -> new ArrayList<>());

            if (old == null) {
                list.add(option.getOption());
            } else {
                list.remove(old.getOption());
                list.add(option.getOption());
            }

            return old;
        }

        public Map<OptionGroup, List<Option>> getOptionsByGroup() {
            return Collections.unmodifiableMap(optionsByGroup);
        }

        public Map<String, OptionSpec> getOptions() {
            return Collections.unmodifiableMap(options);
        }
    }


    // ==============
    // * OptionSpec *
    // ==============

    /**
     * An object used to describe an option.
     * It also contains {@link #arguments} and {@link #present} fields.
     * They serve to the parser: to store arguments before converting and
     * checking if an option is required but not present
     * @author PoulpoGaz
     */
    private static class OptionSpec {

        private final Option option;
        private final String[] names;
        private final boolean optional;
        private final boolean allowDuplicate;
        private final boolean hasArgument;
        private final String defaultValue;
        private final String description;
        private final String argumentName;
        private final Field field;

        private final List<String> arguments = new ArrayList<>();
        private boolean present;

        public OptionSpec(Option option, Field field) {
            this(option, option.names(), option.optional(), option.allowDuplicate(), option.hasArgument(),
                    ArgsUtils.first(option.defaultValue()),
                    ArgsUtils.first(option.description()),
                    ArgsUtils.first(option.argName()),
                    field);
        }

        private OptionSpec(String[] names, boolean optional, boolean allowDuplicate, boolean hasArgument,
                           String defaultValue, String description, String argName, Field field) {
            this(null, names, optional, allowDuplicate, hasArgument, description, defaultValue, argName, field);
        }

        private OptionSpec(Option option, String[] names, boolean optional, boolean allowDuplicate,
                           boolean hasArgument, String defaultValue, String description, String argName, Field field) {
            if (names == null || names.length == 0) {
                throw new IllegalArgumentException("Can't construct an option without a name");
            }

            this.option = option;
            this.names = names;
            this.optional = optional;
            this.allowDuplicate = allowDuplicate;
            this.hasArgument = hasArgument;
            this.defaultValue = defaultValue;
            this.description = description;
            this.argumentName = argName;
            this.field = field;
        }

        public void addArguments(String args) {
            arguments.add(args);
        }

        public void markPresent() {
            present = true;
        }

        public void reset() {
            present = false;
            arguments.clear();
        }

        public Option getOption() {
            return option;
        }

        public String firstName() {
            return names[0];
        }

        public String[] getNames() {
            return names;
        }

        public String getDescription() {
            return description;
        }

        public boolean isOptional() {
            return optional;
        }

        public boolean allowDuplicate() {
            return allowDuplicate;
        }

        public String getArgumentName() {
            return argumentName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public boolean hasArgument() {
            return hasArgument;
        }

        public Field getField() {
            return field;
        }

        public List<String> getArgumentsList() {
            return arguments;
        }

        public boolean isPresent() {
            return present;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OptionSpec that = (OptionSpec) o;

            if (optional != that.optional) return false;
            if (allowDuplicate != that.allowDuplicate) return false;
            if (present != that.present) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(names, that.names)) return false;
            if (!Objects.equals(description, that.description)) return false;
            if (!Objects.equals(defaultValue, that.defaultValue)) return false;
            if (!Objects.equals(argumentName, that.argumentName)) return false;
            return arguments.equals(that.arguments);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(names);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (optional ? 1 : 0);
            result = 31 * result + (allowDuplicate ? 1 : 0);
            result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
            result = 31 * result + (argumentName != null ? argumentName.hashCode() : 0);
            result = 31 * result + arguments.hashCode();
            result = 31 * result + (present ? 1 : 0);
            return result;
        }
    }


    // ========================
    // * CommandDescriberImpl *
    // ========================

    /**
     * Implementation of CommandDescriber
     */
    private static class CommandDescriberImpl implements CommandDescriber {

        private final CommandSpec spec;
        private final Map<OptionGroup, List<Option>> optionsByGroup;

        public CommandDescriberImpl(CommandSpec spec, Map<OptionGroup, List<Option>> optionsByGroup) {
            this.spec = spec;
            this.optionsByGroup = optionsByGroup;
        }

        @Override
        public String getName() {
            return spec.getName();
        }

        @Override
        public String getUsage() {
            return spec.getCommand().getUsage();
        }

        @Override
        public boolean hasDefaultHelp() {
            return spec.getCommand().addHelp();
        }

        @Override
        public OptionIterator optionIterator() {
            return new OptionIteratorImpl(optionsByGroup);
        }

        @Override
        public Option getOption(String name) {
            return spec.getOptions().get(name).getOption();
        }

        @Override
        public int nOptions() {
            return spec.getOptions().size();
        }

        @Override
        public Map<OptionGroup, List<Option>> getOptions() {
            return optionsByGroup;
        }
    }
}
