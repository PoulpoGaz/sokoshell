package fr.valax.args;

import fr.valax.args.api.*;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.INode;
import fr.valax.args.utils.TypeException;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static fr.valax.args.Redirect.NONE;
import static fr.valax.args.Redirect.OutputFile.STD_ERR;
import static fr.valax.args.Redirect.OutputFile.STD_OUT;
import static fr.valax.args.utils.ArgsUtils.*;

/**
 * @author PoulpoGaz
 */
public class CommandLine {

    private String name = "cli";

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

    private InputStream stdIn = System.in;
    private PrintStream stdOut = System.out;
    private PrintStream stdErr = System.err;

    // variables used when executing a command
    private InputStream currIn;
    private PrintStream currOut;
    private PrintStream currErr;

    private Redirect output = Redirect.NONE;
    private Redirect input = Redirect.NONE;

    private ITokenizer tokenizer;

    /**
     * An error due to a redirect.
     * It can be syntax error (eg: missing file after >) or io error (eg: file doesn't exist).
     * These errors are not redirected.
     */
    private String cliError;

    /**
     * An error due to an option.
     * It can be a missing option, not allowed usage of vaargs, etc.
     * These errors are redirected.
     */
    private String optionError;
    private INode<CommandSpec> command;
    private boolean parsingOptions;
    private boolean endParsingOptions;

    CommandLine(INode<Command> root,
                Map<Class<?>, TypeConverter<?>> converters,
                HelpFormatter helpFormatter) throws CommandLineException {
        this.converters = converters;
        this.helpFormatter = helpFormatter;

        this.root = root.mapThrow((c) -> c == null ? null : new CommandSpec(c)).immutableCopy();
        this.commands = this.root.map((c) -> c == null ? null : c.getDescriber());
    }

    public int execute(String[] commandStr) throws CommandLineException, IOException {
        return execute(new TokenizerFromArray(commandStr));
    }

    public int execute(String commandStr) throws CommandLineException, IOException {
        return execute(new Tokenizer(commandStr));
    }

    private int execute(ITokenizer tokenizer) throws CommandLineException, IOException {
        currIn = stdIn;
        currOut = stdOut;
        currErr = stdErr;

        cliError = null;
        command = root;
        parsingOptions = false;

        Redirect.READ_INPUT_UNTIL.setStdIn(stdIn);
        Redirect.READ_INPUT_UNTIL.setStdOut(stdOut);

        this.tokenizer = tokenizer;

        int last = 0;
        while (tokenizer.hasNext()) {

            Token next = tokenizer.next();

            if (next.isRedirect() || next.isCommandSeparator()) {

                Integer v = parseKeyword(next);

                if (v != null) {
                    last = v;
                }

            } else if (next.isEndOfOption() && !endParsingOptions) {
                parsingOptions = true;
                endParsingOptions = true;

            } else if (!hasError()) {

                if (parsingOptions && (next.isWord() || endParsingOptions)) {
                    if (next.isWord() || next.isEndOfOption()) {
                        addVaArgs(next.value());
                    } else {
                        // next is an option, so after it should be a word
                        addVaArgs(next.value() + tokenizer.next().value());
                    }
                } else if (next.isOption()) {
                    parsingOptions = true;

                    if (command.getValue() == null) {
                        cliError = "No root command exists";
                    } else {
                        parseOption(tokenizer.next().value());
                    }

                } else { // find command
                    INode<CommandSpec> sub = subCommand(command, next.value());

                    if (sub == null) {

                        if (command.getValue() == null) {
                            cliError = next.value() + ": command not found";
                        } else {
                            parsingOptions = true;
                            addVaArgs(next.value());
                        }

                    } else {
                        command = sub;
                    }
                }
            }
        }

        if (command.getValue() != null) {
            return executeCommand();
        } else {
            if (cliError != null) {
                printRedirectError();
                return Command.FAILURE;
            } else {
                return last;
            }
        }
    }

    private INode<CommandSpec> subCommand(INode<CommandSpec> node, String name) {
        for (INode<CommandSpec> child : node.getChildren()) {
            if (child.getValue() != null && child.getValue().getName().equals(name)) {
                return child;
            }
        }

        return null;
    }

    private Integer parseKeyword(Token token) throws CommandLineException, IOException {
        switch (token.type()) {
            case Token.WRITE_APPEND ->       redirectOutput(token.value(), STD_OUT, true);
            case Token.WRITE ->              redirectOutput(token.value(), STD_OUT, false);
            case Token.WRITE_ERROR_APPEND -> redirectOutput(token.value(), STD_ERR, true);
            case Token.WRITE_ERROR ->        redirectOutput(token.value(), STD_ERR, false);
            case Token.STD_ERR_IN_STD_OUT -> output = Redirect.ERROR_IN_OUTPUT;
            case Token.READ_INPUT_UNTIL -> {
                if (cliError != null) {
                    return null;
                }

                if (!tokenizer.hasNext()) {
                    cliError = "expecting word after <<";
                    return null;
                }

                Token next = tokenizer.next();
                if (!next.isWord()) {
                    cliError = "expecting word after <<";
                }

                Redirect.READ_INPUT_UNTIL.setUntil(next.value());
                input = Redirect.READ_INPUT_UNTIL;
            }
            case Token.READ_FILE -> {
                if (cliError != null) {
                    return null;
                }

                String file = getFile("<");

                if (file == null) {
                    return null;
                }

                Path path = Path.of(file);
                if (!Files.exists(path)) {
                    cliError = path + ": No such file or directory";
                    return null;
                }

                if (Files.isDirectory(path)) {
                    cliError = path + ": is a directory";
                }

                if (!Files.isReadable(path)) {
                    cliError = path + ": Permission denied";
                }

                input = Redirect.INPUT_FILE;
                Redirect.INPUT_FILE.setPath(path);
            }
            case Token.PIPE -> {
                // a little hard coded

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                currOut = new PrintStream(baos);

                int ret = executeCommand();

                currOut = stdOut;
                currIn = new ByteArrayInputStream(baos.toByteArray());
                input = NONE;

                return ret;
            }
            case Token.COMMAND_SEPARATOR -> {
                return executeCommand();
            }
        }

        return null;
    }

    private void redirectOutput(String keyword, int redirect, boolean append) {
        if (cliError != null) {
            return;
        }

        String file = getFile(keyword);

        if (file == null) {
            return;
        }

        Path path = Path.of(file);

        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                cliError = path + ": is a directory";
                return;
            }

            if (!Files.isWritable(path)) {
                cliError = path + ": Permission denied";
                return;
            }
        }

        output = Redirect.OUTPUT_FILE;
        Redirect.OUTPUT_FILE.set(path, redirect, append);
    }

    /**
     * @return the file after a keyword or null
     */
    private String getFile(String keyword) {
        if (!tokenizer.hasNext()) {
            cliError = "Expecting file after " + keyword;
            return null;
        }

        Token file = tokenizer.next();

        if (!file.isWord()) {
            cliError = "Expecting file after %s".formatted(keyword);
            return null;
        }

        String f = file.value();

        if (f.startsWith("~")) {
            return System.getProperty("user.home") + f.substring(1);
        } else {
            return f;
        }
    }


    private void parseOption(String name) {
        CommandSpec spec = command.getValue();

        OptionSpec opt = spec.findOption(name);

        if (opt == null) {
            optionError = name + ": No such option";
            return;
        }

        addArgumentToOption(opt);
    }

    private void addArgumentToOption(OptionSpec opt) {
        if (opt.isPresent() && !opt.allowDuplicate()) {
            optionError = opt.firstName() + ": Duplicate parameter";
            return;
        }

        if (tokenizer.hasNext()) {
            if (opt.hasArgument()) {
                Token next = tokenizer.next();

                if (!next.isWord()) {
                    optionError = opt.firstName() + ": expecting word";
                    return;
                }

                opt.addArgument(next.value());

            }

        } else if (opt.hasArgument()) {
            if (opt.getDefaultValue() == null || opt.getDefaultValue().isEmpty()) {
                optionError = opt.firstName() + ": Parameter required";
                return;
            } else {
                opt.addArgument(opt.getDefaultValue());
            }
        }

        opt.markPresent();
    }


    private void addVaArgs(String str) {
        CommandSpec spec = command.getValue();

        if (spec.hasVaArgs()) {
            VaArgsSpec vaArgsSpec = spec.getVaargs();

            vaArgsSpec.addValue(str);
        } else {
            optionError = "VaArgs not allowed";
        }
    }


    private int executeCommand() throws CommandLineException, IOException {
        try {
            if (cliError != null) {
                printRedirectError();
                return Command.FAILURE;
            }

            // redirect
            doRedirect();

            CommandSpec spec = command.getValue();
            Command command = spec.getCommand();

            if (optionError == null) {
                optionError = spec.checkNotOptionalOption();
            }

            if (optionError != null) {
                printOptionError();
                return Command.FAILURE;
            }

            // help
            if (command.addHelp() && spec.getHelp().isPresent()) {
                currOut.println(helpFormatter.commandHelp(spec.getDescriber()));

                return Command.SUCCESS;
            } else {

                // reflection
                try {
                    spec.setOptions();
                } catch (TypeException e) {
                    currErr.printf("%s: %s%n", command.getName(), e.getMessage());
                    return Command.FAILURE;
                }

                return command.execute(currIn, currOut, currErr);
            }
        } finally {
            reset();
        }
    }

    private void doRedirect() throws IOException {
        InputStream in = input.redirectIn(currIn);
        PrintStream out = output.redirectOut(currOut, currErr);
        PrintStream err = output.redirectErr(currOut, currErr);

        this.currIn = in;
        this.currOut = out;
        this.currErr = err;
    }

    private void reset() throws IOException {
        if (input == Redirect.INPUT_FILE) {
            currIn.close();
            currIn = stdIn;
        }

        if (output == Redirect.OUTPUT_FILE) {

            if (Redirect.OUTPUT_FILE.isRedirectingStdOut()) {
                currOut.close();
                currOut = stdOut;
            }

            if (Redirect.OUTPUT_FILE.isRedirectingStdErr()) {
                currErr.close();
                currErr = stdErr;
            }

        } else if (output == Redirect.ERROR_IN_OUTPUT) {
            currOut = stdOut;
            currErr = stdErr;
        }

        if (command.getValue() != null) {
            command.getValue().reset();
        }

        output = NONE;
        input = NONE;
        cliError = null;
        optionError = null;
        command = root;
        parsingOptions = false;
        endParsingOptions = false;
    }

    private void printRedirectError() {
        currErr.printf("%s: %s%n", name, cliError);
    }

    private void printOptionError() {
        CommandSpec spec = command.getValue();

        if (spec != null) {
            List<String> names = new ArrayList<>(command.getDepth());

            INode<CommandSpec> node = command;
            while (node != null) {
                if (node.getValue() != null) {
                    names.add(node.getValue().getName());
                }

                node = node.getParent();
            }

            StringBuilder name = new StringBuilder();

            for (int i = names.size() - 1; i >= 0; i--) {
                name.append(names.get(i));

                if (i - 1 >= 0) {
                    name.append(" ");
                }
            }

            currErr.printf("%s: %s%n", name, optionError);
        } else {
            currErr.printf("'root command': %s%n", optionError);
        }
    }

    private boolean hasError() {
        return cliError != null || optionError != null;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> getConverter(Class<T> class_) {
        return (TypeConverter<T>) converters.get(class_);
    }

    public String getGeneralHelp() {
        return helpFormatter.generalHelp(commands);
    }

    public String getCommandHelp(String command) {
        INode<CommandDescriber> node = getFirstCommand(command);

        if (node != null && node.getValue() != null) {
            return helpFormatter.commandHelp(node.getValue());
        } else {
            return "%s: %s: command not found".formatted(name, command);
        }
    }

    public INode<CommandDescriber> getCommands() {
        return commands;
    }

    public INode<CommandDescriber> getFirstCommand(String command) {
        ParsedCommand first = getFirstCommandWithIndex(command);

        if (first != null) {
            return first.node();
        } else {
            return null;
        }
    }

    public ParsedCommand getFirstCommandWithIndex(String command) {
        int index = 0;
        Tokenizer tokenizer = new Tokenizer(command);

        INode<CommandSpec> cmd = root;
        while (tokenizer.hasNext()) {
            Token next = tokenizer.next();

            if (next.isCommandSeparator()) {
                break;
            } else if (next.isRedirect()) {
                if (tokenizer.hasNext()) {
                    tokenizer.next();
                }

                continue;
            }

            INode<CommandSpec> sub = subCommand(cmd, next.value());

            if (sub == null) {
                break;
            } else {
                cmd = sub;
            }

            index++;
        }

        if (cmd.getValue() == null) {
            return null;
        }

        INode<CommandDescriber> desc = commands.find(cmd.getValue().getDescriber());

        return new ParsedCommand(desc, index);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputStream getStdIn() {
        return stdIn;
    }

    public void setStdIn(InputStream stdIn) {
        this.stdIn = stdIn;
    }

    public PrintStream getStdOut() {
        return stdOut;
    }

    public void setStdOut(PrintStream stdOut) {
        this.stdOut = stdOut;
    }

    public PrintStream getStdErr() {
        return stdErr;
    }

    public void setStdErr(PrintStream stdErr) {
        this.stdErr = stdErr;
    }

    //
    //
    //

    public record ParsedCommand(INode<CommandDescriber> node, int index) {}

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

        private final Command command;
        private final CommandDescriber describer;

        private final Map<String, OptionSpec> options;
        private final OptionSpec help;
        private final VaArgsSpec vaargs;

        public CommandSpec(Command command) throws CommandLineException {
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

        public OptionSpec findOption(String name) {
            OptionSpec spec = options.get(name);

            if (spec == null) {
                if (name.length() == 1) {
                    char c = name.charAt(0);

                    spec = options.values()
                            .stream()
                            .filter((o) -> ArgsUtils.contains(o.getShortNames(), c))
                            .findFirst()
                            .orElse(null);
                } else {
                    spec = options.values()
                            .stream()
                            .filter((o) -> ArgsUtils.contains(o.getLongNames(), name))
                            .findFirst()
                            .orElse(null);
                }
            }

            return spec;
        }

        public String checkNotOptionalOption() {
            for (OptionSpec opt : options.values()) {
                if (!opt.isOptional() && !opt.isPresent()) {
                    return opt.firstName() + ": required";
                }
            }

            return null;
        }

        public void reset() {
            for (OptionSpec opt : options.values()) {
                opt.reset();
            }

            if (vaargs != null) {
                vaargs.reset();
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
                        } else if (opt.getDefaultValue() == null) {
                            setValue(field, converter, null);
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

            if (hasVaArgs()) {
                setArray(vaargs.getField(), vaargs.getConverter(), vaargs.getValues().toArray(new String[0]));
            }
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

            // avoid casting to Object[] as it throws ClassCastException when it's an array of primitives
            Object array = Array.newInstance(type, values.length);

            for (int i = 0; i < values.length; i++) {
                Array.set(array, i, converter.convert(values[i]));
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

        public Command getCommand() {
            return command;
        }

        public boolean hasVaArgs() {
            return vaargs != null;
        }

        public VaArgsSpec getVaargs() {
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

        private final char[] shortNames;
        private final String[] longNames;

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

            List<Character> shortNames = new ArrayList<>();
            List<String> longNames = new ArrayList<>();

            for (String name : option.names()) {
                if (name.length() == 1) {
                    shortNames.add(name.charAt(0));
                } else if (name.length() > 1) {
                    longNames.add(name);
                } else {
                    thrExc("Empty name in Option for field %s", field.getName());
                }
            }

            this.shortNames = asCharArray(shortNames);
            this.longNames = longNames.toArray(new String[0]);
        }

        public void addArgument(String args) {
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

        public String[] getLongNames() {
            return longNames;
        }

        public char[] getShortNames() {
            return shortNames;
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

        private final List<String> values = new ArrayList<>();

        public VaArgsSpec(VaArgs vaArgs, Field field) throws CommandLineException {
            this.vaArgs = vaArgs;
            this.field = field;
            this.converter = createTypeConverter(field, first(vaArgs.converter()));
        }

        public void addValue(String value) {
            values.add(value);
        }

        public void reset() {
            values.clear();
        }

        public List<String> getValues() {
            return values;
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
                Constructor<? extends TypeConverter<?>> constructor = customConverter.getDeclaredConstructor();
                constructor.setAccessible(true);

                return constructor.newInstance();
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
        public Command getCommand() {
            return spec.getCommand();
        }

        @Override
        public String getName() {
            return spec.getName();
        }

        @Override
        public String getShortDescription() {
            return spec.getCommand().getShortDescription();
        }

        @Override
        public String[] getUsage() {
            return spec.getCommand().getUsage();
        }

        @Override
        public boolean hasDefaultHelp() {
            return spec.getCommand().addHelp();
        }

        @Override
        public boolean hasVaArgs() {
            return spec.hasVaArgs();
        }

        @Override
        public String getVaArgsDescription() {
            return ArgsUtils.first(spec.getVaargs().vaArgs.description());
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
            return new String[] {"h", "help"};
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
