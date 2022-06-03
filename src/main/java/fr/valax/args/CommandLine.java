package fr.valax.args;

import fr.valax.args.api.*;
import fr.valax.args.utils.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

import static fr.valax.args.utils.ArgsUtils.*;

/**
 * @author PoulpoGaz
 */
public class CommandLine {

    /**
     * Contains all commands in a tree
     * Only the root node is allowed to have null value
     */
    private final INode<CommandSpec> root;
    private final INode<CommandDescriber> commands;

    /**
     * A class with type T must be associated with a type converted of the same type
     */
    private final Map<Class<?>, TypeConverter<?>> converters;

    private final HelpFormatter helpFormatter;
    private boolean showHelp = true;

    CommandLine(INode<Command<?>> root,
                Map<Class<?>, TypeConverter<?>> converters,
                HelpFormatter helpFormatter) throws CommandLineException {
        this.converters = converters;
        this.helpFormatter = helpFormatter;

        this.root = root.mapThrow((c) -> c == null ? null : new CommandSpec(c)).immutableCopy();
        this.commands = this.root.map((c) -> c == null ? null : c.getDescriber());
    }

    public Object execute(String[] args) throws CommandLineException {
        ParsedCommand parsedCommand = getCommand(root, args, 0);

        if (parsedCommand.node() == null) {
            unrecognizedCommand(args);
            return null;
        } else {
            CommandSpec spec = parsedCommand.node().getValue();

            return executeCommand(spec, args, parsedCommand.index(), args.length);
        }
    }

    private ParsedCommand getCommand(INode<CommandSpec> node, String[] command, int index) {
        if (index >= command.length) {
            CommandSpec spec = node.getValue();

            if (spec == null) { // for root
                return new ParsedCommand(null, index);
            } else {
                return new ParsedCommand(node, index);
            }

        } else {
            INode<CommandSpec> next = null;
            for (INode<CommandSpec> child : node.getChildren()) {
                CommandSpec spec = child.getValue();
                if (command[index].equals(spec.getName())) {
                    next = child;
                    break;
                }
            }

            if (next != null) {
                return getCommand(next, command, index + 1);
            } else if (node.getValue() != null) {
                return new ParsedCommand(node, index);
            } else {
                return new ParsedCommand(null, index);
            }
        }
    }

    private void unrecognizedCommand(String[] args) throws ParseException {
        if (showHelp()) {
            System.out.println(helpFormatter.generalHelp(commands, args, true));
        } else {
            thrParseExc("Unrecognized command: %s", Arrays.toString(args));
        }
    }

    protected Object executeCommand(CommandSpec spec, String[] args, int start, int end)
            throws CommandLineException {
        try {


            try {
                if (spec.hasVaArgs()) {
                    List<String> vaArgs = spec.parseAllowVaArgs(args, start, end);

                    spec.setVaArgs(vaArgs);
                } else {
                    spec.parse(args, start, end);
                }
            } catch (ParseException e) {
                if (showHelp) {
                    System.out.println(helpFormatter.commandHelp(e, spec.getDescriber()));
                    return null;
                } else {
                    throw e;
                }
            }

            if (spec.getCommand().addHelp() && spec.getHelp().isPresent()) {
                System.out.println(helpFormatter.commandHelp(null, spec.getDescriber()));

                return null;
            } else {
                try {
                    spec.setOptions();
                } catch (TypeException e) {
                    if (showHelp) {
                        System.out.println(helpFormatter.commandHelp(e, spec.getDescriber()));
                        return null;
                    } else {
                        throw e;
                    }
                }


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
        return helpFormatter.generalHelp(commands, null, false);
    }

    public String getCommandHelp(String command) {
        INode<CommandSpec> node;

        if (command.isBlank()) {
            node = root;
        } else {
            String[] split = command.split(" ");

            node = getCommandHelp(root, split, 0);
        }

        if (node != null && node.getValue() != null) {
            return helpFormatter.commandHelp(null, node.getValue().getDescriber());
        } else {
            return "Unknown command: " + command;
        }
    }

    private INode<CommandSpec> getCommandHelp(INode<CommandSpec> node, String[] parts, int index) {
        if (index < parts.length) {
            for (INode<CommandSpec> child : node.getChildren()) {

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

    public INode<CommandDescriber> getCommands() {
        return commands;
    }

    public ParsedCommandDesc getCommand(String[] command) {
        return convert(getCommand(root, command, 0));
    }

    public ParsedCommandDesc getCommand(String command) {
        return getCommand(splitQuoted(command));
    }

    // =================
    // * Inner classes *
    // =================

    private record ParsedCommand(INode<CommandSpec> node, int index) {}

    public record ParsedCommandDesc(INode<CommandDescriber> node, int index) {}

    private ParsedCommandDesc convert(ParsedCommand cmd) {
        if (cmd.node() == null) {
            return new ParsedCommandDesc(null, cmd.index());
        } else {
            CommandDescriber desc = cmd.node().getValue().getDescriber();

            return new ParsedCommandDesc(commands.find(desc), cmd.index());
        }
    }

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
        private final VaArgsSpec vaargs;

        public CommandSpec(Command<?> command) throws CommandLineException {
            Objects.requireNonNull(command.getName());
            this.command = command;

            OptionsBuilder builder = new OptionsBuilder();

            createOptions(builder);
            if (command.addHelp()) {
                help = new OptionSpec(HELP_OPTION, null);

                if (builder.addOption(help) != null) {
                    thrExc("Two option have same name");
                }
            } else {
                help = null;
            }

            this.options = builder.getOptions();
            this.vaargs = builder.getVaArgsSpec();
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
                    if (vaargs != null) {
                        thrExc("Command can't have two 'VaArgs' (%s)", getName());
                    }

                    checkNotFinal(field);
                    checkArray(field);
                    field.setAccessible(true);

                    VaArgs v = field.getAnnotation(VaArgs.class);

                    builder.setVaArgsSpec(new VaArgsSpec(v, field));
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
                    String withoutHyphen = arg.substring(1);
                    option = options.get(withoutHyphen);

                    if (option == null) {
                        option = findOption(withoutHyphen);
                    }

                    if (option == null) {
                        throw new ParseException("Unrecognized option: " + arg);
                    }

                    i = parseOption(option, args, i, end);
                } else {
                    vaargs.add(arg);
                }
            }

            if (help == null || !help.isPresent()) {
                checkNotOptionalOption();
            }

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

        private OptionSpec findOption(String name) {
            return options.values()
                    .stream()
                    .filter((o) -> ArgsUtils.contains(o.getOption().names(), name))
                    .findFirst()
                    .orElse(null);
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

                // only the built-in help is allowed to have a null field
                if (opt.getField() == null) {
                    continue;
                }

                Field field = opt.getField();

                if (opt.hasArgument()) {
                    TypeConverter<?> converter = Objects.requireNonNull(opt.getTypeConverter());

                    List<String> args = opt.getArgumentsList();

                    if (opt.allowDuplicate()) {
                        if (opt.isPresent()) {
                            setArray(field, converter, args.toArray(new String[0]));
                        } else {
                            setArray(field, converter, new String[] {opt.getDefaultValue()});
                        }
                    } else {
                        if (opt.isPresent()) {
                            setValue(field, converter, args.get(0));
                        } else {
                            setValue(field, converter, opt.getDefaultValue());
                        }
                    }
                } else {
                    setValue(field, opt.isPresent());
                }
            }
        }

        public void setVaArgs(List<String> vaArgs) throws CommandLineException {
            Objects.requireNonNull(vaArgs);
            setArray(vaargs.getField(), vaargs.getConverter(), vaArgs.toArray(new String[0]));
        }

        private void setValue(Field field, TypeConverter<?> converter, String value) throws CommandLineException {
            Object o = converter.convert(value);
            setValue(field, o);
        }

        /**
         * Some black magic
         */
        private void setArray(Field field, TypeConverter<?> converter, String[] values) throws CommandLineException {
            Class<?> type = field.getType().getComponentType();

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

        public boolean hasVaArgs() {
            return vaargs != null;
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

        private VaArgsSpec vaArgsSpec;

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

        public VaArgsSpec getVaArgsSpec() {
            return vaArgsSpec;
        }

        public void setVaArgsSpec(VaArgsSpec vaArgsSpec) {
            this.vaArgsSpec = vaArgsSpec;
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
    private class OptionSpec {

        private final Option option;
        private final Field field;
        private final TypeConverter<?> converter;

        private final List<String> arguments = new ArrayList<>();
        private boolean present;

        public OptionSpec(Option option, Field field) throws CommandLineException {
            this.option = option;
            this.field = field;

            if (option.hasArgument()) {
                converter = createTypeConverter(field, first(option.converter()));
            } else {
                converter = null;
            }
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
            return first(option.names());
        }

        public boolean isOptional() {
            return option.optional();
        }

        public boolean allowDuplicate() {
            return option.allowDuplicate();
        }

        public String getDefaultValue() {
            return first(option.defaultValue());
        }

        public boolean hasArgument() {
            return option.hasArgument();
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

        public TypeConverter<?> getTypeConverter() {
            return converter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OptionSpec that = (OptionSpec) o;
            return option.equals(that.option);
        }

        @Override
        public int hashCode() {
            return Objects.hash(option);
        }
    }

    // ==============
    // * VaArgsSpec *
    // ==============

    public class VaArgsSpec {

        private final VaArgs vaArgs;
        private final Field field;
        private final TypeConverter<?> converter;

        public VaArgsSpec(VaArgs vaArgs, Field field) throws CommandLineException {
            this.vaArgs = vaArgs;
            this.field = field;
            this.converter = createTypeConverter(field, first(vaArgs.converter()));
        }

        public VaArgs getVaArgs() {
            return vaArgs;
        }

        public Field getField() {
            return field;
        }

        public TypeConverter<?> getConverter() {
            return converter;
        }
    }

    //
    //
    //

    private TypeConverter<?> createTypeConverter(Field field,
                                                 Class<? extends TypeConverter<?>> customConverter)
            throws CommandLineException {
        if (field == null) {
            return null;
        }

        try {
            if (customConverter != null) {
                return customConverter.getDeclaredConstructor().newInstance();
            } else {
                Class<?> type = field.getType();

                TypeConverter<?> converter;
                if (type.isArray()) {
                    converter = getConverter(type.getComponentType());
                } else {
                    converter = getConverter(type);
                }

                notNull(converter, "Can't find converter for %s", field);

                return converter;
            }

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new CommandLineException(e);
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
        public Command<?> getCommand() {
            return spec.getCommand();
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
            OptionSpec optSpec = spec.getOptions().get(name);

            if (optSpec == null) {
                return null;
            } else {
                return optSpec.getOption();
            }
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

    // Don't know if it's a good or bad practice to instantiate an annotation
    private static final Option HELP_OPTION = new Option() {
        @Override
        public String[] names() {
            return new String[] {"h", "-help"};
        }

        @Override
        public boolean optional() {
            return true;
        }

        @Override
        public boolean allowDuplicate() {
            return false;
        }

        @Override
        public boolean hasArgument() {
            return false;
        }

        @Override
        public String[] defaultValue() {
            return new String[0];
        }

        @Override
        public String[] description() {
            return new String[] {"Print help"};
        }

        @Override
        public String[] argName() {
            return new String[0];
        }

        @Override
        public Class<? extends TypeConverter<?>>[] converter() {
            return new Class[0];
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Option.class;
        }
    };
}
