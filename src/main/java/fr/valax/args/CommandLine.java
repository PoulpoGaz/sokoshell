package fr.valax.args;

import fr.valax.args.utils.Node;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;

public class CommandLine {

    private static final OptionSpec DEFAULT_HELP = new OptionSpecBuilder()
            .name("h").name("-h")
            .desc("Print this message and exit")
            .build();

    private final Map<String, Node<CommandSpec>> commands;
    // Only the root node is allowed to have null value
    private final Node<CommandSpec> root;

    /** A class with type T must be associated with a type converted of the same typ */
    private final Map<Class<?>, TypeConverter<?>> converters;

    private OptionSpec help = DEFAULT_HELP;

    public CommandLine() {
        commands = new HashMap<>();
        root = new Node<>();
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

    public void parse(String[] args) throws CommandLineException {
        parseSub(root, args, 0);
    }

    public void parseSub(Node<CommandSpec> parent, String[] args, int index) throws CommandLineException {
        if (index >= args.length) {
            CommandSpec spec = parent.getValue();

            if (spec == null) { // for root
                thrExc("Unrecognized command: %s", Arrays.toString(args));
            } else {
                execute(spec, args, args.length, args.length);
            }

        } else {
            Node<CommandSpec> next = null;
            for (Node<CommandSpec> child : parent.getChildren()) {
                CommandSpec spec = child.getValue();
                if (args[index].equals(spec.getName())) {
                    next = child;
                    break;
                }
            }

            if (next != null) {
                parseSub(next, args, index + 1);
            } else if (parent.getValue() != null) {
                execute(parent.getValue(), args, index, args.length);
            } else {
                thrExc("Unrecognized command: %s", Arrays.toString(args));
            }
        }
    }

    protected void execute(CommandSpec spec, String[] args, int start, int end) throws CommandLineException {
        try {
            if (spec.vaargs != null) {
                List<String> vaArgs = spec.options.parseAllowVaArgs(args, start, end);

                spec.setVaArgs(vaArgs);
            } else {
                spec.options.parse(args, start, end);
            }
        } catch (ParseException e) {
            if (spec.command.help()) {
                HelpFormatter.printHelp(spec.action);
                return;
            } else {
                throw new CommandLineException("In " + spec.getName(), e);
            }
        }

        spec.setOptions();
        spec.action.run();
    }

    protected Node<CommandSpec> getSpecFromClass(Class<?> class_) {
        for (Node<CommandSpec> s : commands.values()) {
            if (s.getValue().getAction().getClass().equals(class_)) {
                return s;
            }
        }

        return null;
    }

    public CommandLine addCommand(Runnable command) throws CommandLineException {
        if (!command.getClass().isAnnotationPresent(Command.class)) {
            thrExc("Command annotation isn't present in %s".formatted(command.getClass().getSimpleName()));
        }

        CommandSpec spec = new CommandSpec(command);
        Node<CommandSpec> node = new Node<>(spec);

        Node<CommandSpec> old = commands.put(spec.getName(), node);
        if (old != null) {
            old.removeFromParent();
            commands.remove(spec.getName());

            for (Node<CommandSpec> s : old) {
                commands.remove(s.getValue().getName());
            }
        }

        if (spec.getParent() == null) {
            root.addChildren(node);
        } else {
            Node<CommandSpec> parent = getSpecFromClass(spec.getParent());
            parent.addChildren(node);
        }

        return this;
    }

    public void removeCommand(String name) {
        commands.remove(name);
    }

    public Runnable getCommand(String name) {
        Node<CommandSpec> node = commands.get(name);

        if (node == null) {
            return null;
        } else {
            return node.getValue().getAction();
        }
    }

    public List<Runnable> getCommands() {
        return commands.values()
                .stream()
                .filter(n -> Objects.nonNull(n.getValue()))
                .map((n) -> n.getValue().getAction())
                .toList();
    }

    public <T> CommandLine addConverter(Class<T> class_, TypeConverter<T> converter) {
        converters.put(class_, converter);

        return this;
    }

    public <T> void removeConverter(Class<T> class_, TypeConverter<?> converter) {
        converters.remove(class_, converter);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> getConverter(Class<T> class_) {
        return (TypeConverter<T>) converters.get(class_);
    }

    public Map<Class<?>, TypeConverter<?>> getConverters() {
        return Collections.unmodifiableMap(converters);
    }

    public OptionSpec getHelp() {
        return help;
    }

    public void setHelp(OptionSpec help) {
        this.help = help;
    }

    private class CommandSpec {

        private final Command command;
        private final Runnable action;

        private Options options;
        private Map<OptionSpec, Field> optionMapping;
        private Field vaargs;

        public CommandSpec(Runnable action) throws CommandLineException {
            this.action = action;

            Class<?> class_ = action.getClass();
            command = class_.getAnnotation(Command.class);
            createOptions();

            if (command.help()) {
                options.addOption(help);
            }
        }

        protected void createOptions() throws CommandLineException {
            Class<?> class_ = action.getClass();

            options = new Options();
            optionMapping = new HashMap<>();

            for (Field field : getAllFields(class_)) {
                String optGroup = null;
                if (field.isAnnotationPresent(OptionGroup.class)) {
                    optGroup = field.getAnnotation(OptionGroup.class).name();
                }

                if (field.isAnnotationPresent(Option.class)) {
                    field.setAccessible(true);
                    checkNotFinal(field);

                    Option option = field.getAnnotation(Option.class);

                    if (option.allowDuplicate()) {
                        checkArray(field);
                    } else {
                        checkNotArray(field);
                    }

                    if (option.argName().equals("")) {
                        checkBoolean(field, "%s should be a boolean", field.getName());
                    }

                    OptionSpec spec = new OptionSpec(option);
                    options.addOption(optGroup, spec);
                    optionMapping.put(spec, field);

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

        protected List<Field> getAllFields(Class<?> class_) {
            List<Field> fields = new ArrayList<>();

            Class<?> target = class_;
            while (!target.equals(Object.class)) {

                fields.addAll(List.of(target.getDeclaredFields()));

                target = target.getSuperclass();
            }

            return fields;
        }

        public void setOptions() throws CommandLineException {
            for (Map.Entry<OptionSpec, Field> entry : optionMapping.entrySet()) {
                OptionSpec opt = entry.getKey();
                Field field = entry.getValue();

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

        protected TypeConverter<?> getTypeConverterFor(Field field) {
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
                    return converters.get(type.getComponentType());
                } else {
                    return converters.get(type);
                }
            } else {
                return converter;
            }
        }

        protected void setValue(Field field, String value) throws CommandLineException {
            TypeConverter<?> converter = getTypeConverterFor(field);
            notNull(converter, "Can't find converter for %s", vaargs);

            Object o = converter.convert(value);
            setValue(field, o);
        }

        /**
         * Some black magic
         */
        protected void setArray(Field field, String[] values) throws CommandLineException {
            Class<?> type = field.getType().getComponentType();
            TypeConverter<?> converter = getTypeConverterFor(field);
            notNull(converter, "Can't find converter for %s", vaargs);

            Object[] array = (Object[]) Array.newInstance(type, values.length);

            for (int i = 0; i < values.length; i++) {
                array[i] = converter.convert(values[i]);
            }

            setValue(field, array);
        }

        protected void setValue(Field field, Object value) throws CommandLineException {
            try {
                field.set(action, value);
            } catch (IllegalAccessException e) {
                throw new CommandLineException(e);
            }
        }

        protected void checkNotFinal(Field field) throws CommandLineException {
            if (Modifier.isFinal(field.getModifiers())) {
                thrExc("%s is final (in %s)", field.getName(), getName());
            }
        }

        protected void checkArray(Field field) throws CommandLineException {
            if (!field.getType().isArray()) {
                thrExc("%s should be an array (in %s)", field.getName(), getName());
            }
        }

        protected void checkNotArray(Field field) throws CommandLineException {
            if (field.getType().isArray()) {
                thrExc("%s shouldn't be an array (in %s)", field.getName(), getName());
            }
        }
        /**
         * @param field check if the type of field is boolean or array of boolean
         */
        protected void checkBoolean(Field field, String err, Object... args) throws CommandLineException {
            Class<?> type = field.getType();
            if (type.isArray()) {
                type = type.getComponentType();
            }

            if (!type.isAssignableFrom(boolean.class)) {
                thrExc(err, args);
            }
        }

        public String getName() {
            return command.name();
        }

        public Command getCommand() {
            return command;
        }

        public Runnable getAction() {
            return action;
        }

        public boolean hasSubCommands() {
            return command.subCommands().length > 0;
        }

        public Class<?>[] getSubCommands() {
            return command.subCommands();
        }

        public Class<?> getParent() {
            Class<?> p = command.parent();
            return p.equals(Object.class) ? null : p;
        }
    }

    protected static void notNull(TypeConverter<?> converter, String err, Object... args) throws CommandLineException {
        if (converter == null) {
            thrExc(err, args);
        }
    }

    protected static void thrExc(String format, Object... args) throws CommandLineException {
        throw new CommandLineException(format.formatted(args));
    }
}
