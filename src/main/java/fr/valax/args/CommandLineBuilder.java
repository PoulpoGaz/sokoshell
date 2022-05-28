package fr.valax.args;

import fr.valax.args.api.Command;
import fr.valax.args.api.HelpFormatter;
import fr.valax.args.api.TypeConverter;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.INode;
import fr.valax.args.utils.Node;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PoulpoGaz
 */
public class CommandLineBuilder {

    private final CommandLineBuilder ancestor;
    private final INode<Command<?>> root;

    /** not null only for the "root" CommandLineBuilder*/
    private final Map<Class<?>, TypeConverter<?>> converters;
    private HelpFormatter helpFormatter;

    public CommandLineBuilder() {
        this(null, new Node<>());
    }

    public CommandLineBuilder(Command<?> root) {
        this(null, new Node<>(root));
    }

    private CommandLineBuilder(CommandLineBuilder ancestor, INode<Command<?>> root) {
        this.ancestor = ancestor;
        this.root = root;

        if (ancestor == null) {
            converters = new HashMap<>();
        } else {
            converters = null;
        }
    }

    public CommandLine build() throws CommandLineException {
        if (ancestor != null) {
            throw new IllegalStateException("Not the root");
        }

        CommandLine cli;
        if (helpFormatter != null) {
            cli = new CommandLine(root, converters, helpFormatter);
        } else {
            cli = new CommandLine(root, converters, new DefaultHelpFormatter());
        }

        return cli;
    }

    public CommandLineBuilder addCommand(Command<?> c) {
        root.addChild(c);
        return this;
    }

    public CommandLineBuilder subCommand(Command<?> c) {
        return new CommandLineBuilder(this, root.addChild(c));
    }

    public CommandLineBuilder endSubCommand() {
        return ancestor;
    }

    public CommandLineBuilder addDefaultConverters() {
        if (converters == null) {
            throw new IllegalStateException("Not the root");
        }

        converters.put(String.class, TypeConverters.STRING);

        converters.put(Byte.class, TypeConverters.BYTE);
        converters.put(Short.class, TypeConverters.SHORT);
        converters.put(Integer.class, TypeConverters.INT);
        converters.put(Long.class, TypeConverters.LONG);
        converters.put(Float.class, TypeConverters.FLOAT);
        converters.put(Double.class, TypeConverters.DOUBLE);

        converters.put(byte.class, TypeConverters.BYTE_PRIMITIVE);
        converters.put(short.class, TypeConverters.SHORT_PRIMITIVE);
        converters.put(int.class, TypeConverters.INT_PRIMITIVE);
        converters.put(long.class, TypeConverters.LONG_PRIMITIVE);
        converters.put(float.class, TypeConverters.FLOAT_PRIMITIVE);
        converters.put(double.class, TypeConverters.DOUBLE_PRIMITIVE);

        converters.put(Path.class, TypeConverters.PATH);

        return this;
    }

    public <T> CommandLineBuilder addConverter(Class<T> class_, TypeConverter<T> converter) {
        if (converters == null) {
            throw new IllegalStateException("Not the root");
        }

        converters.put(class_, converter);

        return this;
    }

    public <T> CommandLineBuilder removeConverter(Class<T> class_, TypeConverter<?> converter) {
        if (converters == null) {
            throw new IllegalStateException("Not the root");
        }

        converters.remove(class_, converter);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> getConverter(Class<T> class_) {
        if (converters == null) {
            throw new IllegalStateException("Not the root");
        }

        return (TypeConverter<T>) converters.get(class_);
    }

    public Map<Class<?>, TypeConverter<?>> getConverters() {
        if (converters == null) {
            throw new IllegalStateException("Not the root");
        }

        return Collections.unmodifiableMap(converters);
    }

    public HelpFormatter getHelpFormatter() {
        return helpFormatter;
    }

    public CommandLineBuilder setHelpFormatter(HelpFormatter helpFormatter) {
        if (ancestor != null) {
            throw new IllegalStateException("Not the root");
        }

        this.helpFormatter = helpFormatter;
        return this;
    }
}
