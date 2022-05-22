package fr.valax.args;

import fr.valax.args.api.Command;
import fr.valax.args.utils.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CommandLineBuilder {

    private final CommandLineBuilder ancestor;
    private final Node<CommandSpecification> root;

    /** not null only for the "root" CommandLineBuilder*/
    private final Map<Class<?>, TypeConverter<?>> converters;

    public CommandLineBuilder() {
        this(null, new Node<>());
    }

    public CommandLineBuilder(Command<?> root) throws CommandLineException {
        this(null, new Node<>(new CommandSpecification(root)));
    }

    private CommandLineBuilder(CommandLineBuilder ancestor, Node<CommandSpecification> root) {
        this.ancestor = ancestor;
        this.root = root;

        if (ancestor == null) {
            converters = new HashMap<>();
        } else {
            converters = null;
        }
    }

    public CommandLine build() {
        if (ancestor != null) {
            throw new IllegalStateException("Not the root");
        }

        CommandLine cli = new CommandLine(root);

        for (Node<CommandSpecification> spec : root) {
            if (spec.getValue() != null) {
                spec.getValue().setCli(cli);
            }
        }

        return cli;
    }

    public CommandLineBuilder addCommand(Command<?> c) throws CommandLineException {
        root.addChildren(new CommandSpecification(c));
        return this;
    }

    protected CommandLineBuilder subCommand(Command<?> c) throws CommandLineException {
        CommandSpecification spec = new CommandSpecification(c);
        return new CommandLineBuilder(this, root.addChildren(spec));
    }

    public CommandLineBuilder endSubCommand() {
        return ancestor;
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
}
