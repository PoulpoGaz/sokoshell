package fr.valax.args;

import fr.valax.args.annotation.Command;
import fr.valax.args.annotation.Option;
import fr.valax.args.annotation.OptionGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class Options implements Iterable<OptionSpec> {

    public static Options createFrom(Runnable runnable) {
        if (runnable.getClass().isAnnotationPresent(Command.class)) {
            Class<?> class_ = runnable.getClass();

            Options options = new Options();

            for (Field field : class_.getDeclaredFields()) {
                String optGroup = null;
                if (field.isAnnotationPresent(OptionGroup.class)) {
                    optGroup = field.getAnnotation(OptionGroup.class).name();
                }

                if (field.isAnnotationPresent(Option.class)) {
                    Option option = field.getAnnotation(Option.class);

                    OptionSpec spec = new OptionSpec(option);
                    options.addOption(optGroup, spec);

                }
            }

            return options;
        } else {
            throw new IllegalStateException("Runnable isn't annotated with Command");
        }
    }

    private final LinkedHashMap<String, OptionGroupSpec> options;

    public Options() {
        options = new LinkedHashMap<>();
    }

    public Options(Options... opts) {
        if (opts.length == 0) {
            options = new LinkedHashMap<>();
        } else {
            options = new LinkedHashMap<>(opts[0].getOptions());

            for (int i = 1; i < opts.length; i++) {
                merge(opts[i]);
            }
        }
    }

    public void parse(String[] args) throws ParseException {
        parse(args, 0, args.length);
    }

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
                option = getOption(arg.substring(1));

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

    protected int parseOption(OptionSpec option, String[] args, int i, int end) throws ParseException {
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

    protected void checkNotOptionalOption() throws ParseException {
        for (OptionSpec opt : this) {
            if (!opt.isOptional() && !opt.isPresent()) {
                throw new ParseException(opt.firstName() + " is required");
            }
        }
    }

    /**
     * The option will be added to the unnamed group
     * @param option the option to add
     */
    public Options addOption(OptionSpec option) {
        return addOption(null, option);
    }

    public Options addOptionGroup(OptionGroupSpec group) {
        options.put(group.getName(), group);
        return this;
    }

    public Options addOption(String group, OptionSpec option) {
        OptionGroupSpec optGroup = options.get(group);

        if (optGroup == null) {
            optGroup = new OptionGroupSpec(group);
            optGroup.addOption(option);
            options.put(group, optGroup);
        } else {
            optGroup.addOption(option);
        }
        return this;
    }

    public OptionGroupSpec getGroup(String name) {
        return options.get(name);
    }

    public boolean contains(OptionSpec option) {
        for (OptionGroupSpec group : options.values()) {
            if (group.contains(option)) {
                return true;
            }
        }

        return false;
    }

    public void merge(Options b) {
        for (OptionGroupSpec group : b.getOptions().values()) {
            options.merge(group.getName(), group, OptionGroupSpec::new);
        }
    }

    public LinkedHashMap<String, OptionGroupSpec> getOptions() {
        return options;
    }

    public OptionSpec getOption(String name) {
        for (OptionGroupSpec group : options.values()) {
            OptionSpec opt = group.getOption(name);

            if (opt != null) {
                return opt;
            }
        }

        return null;
    }

    public int nOptions() {
        int size = 0;
        for (OptionGroupSpec g : options.values()) {
            size += g.nOptions();
        }
        return size;
    }

    public boolean isEmpty() {
        return nOptions() == 0;
    }

    @Override
    public Iterator<OptionSpec> iterator() {
        return new Iterator<>() {
            private final Iterator<OptionGroupSpec> groupIterator = options
                    .values()
                    .iterator();

            private Iterator<OptionSpec> optionIterator = null;

            @Override
            public boolean hasNext() {
                if (optionIterator == null) {
                    if (groupIterator.hasNext()) {
                        optionIterator = groupIterator.next()
                                .iterator();
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return groupIterator.hasNext();
                }
            }

            @Override
            public OptionSpec next() {
                OptionSpec opt = optionIterator.next();

                if (!optionIterator.hasNext()) {
                    optionIterator = null;
                }

                return opt;
            }
        };
    }
}
