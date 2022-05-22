package fr.valax.args;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * An object containing all options and groups for a command.
 * It also parses arguments of a command
 */
public class Options implements Iterable<OptionSpecification> {

    /*public static Options createFrom(Object object) {
        if (object.getClass().isAnnotationPresent(Command.class)) {
            Class<?> class_ = object.getClass();

            Options options = new Options();

            for (Field field : class_.getDeclaredFields()) {
                String optGroup = null;
                if (field.isAnnotationPresent(OptionGroup.class)) {
                    optGroup = field.getAnnotation(OptionGroup.class).name();
                }

                if (field.isAnnotationPresent(Option.class)) {
                    Option option = field.getAnnotation(Option.class);

                    OptionSpecification spec = new OptionSpecification(option);
                    options.addOption(optGroup, spec);

                }
            }

            return options;
        } else {
            throw new IllegalStateException("The object isn't annotated with Command");
        }
    }*/

    /**
     * An option without a group is added to the OptionGroupSpecification
     * with name = null, also named "unnamed group"
     */
    private final LinkedHashMap<String, OptionGroupSpecification> options;

    public Options() {
        options = new LinkedHashMap<>();
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

            OptionSpecification option;
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

    protected int parseOption(OptionSpecification option, String[] args, int i, int end) throws ParseException {
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
        for (OptionSpecification opt : this) {
            if (!opt.isOptional() && !opt.isPresent()) {
                throw new ParseException(opt.firstName() + " is required");
            }
        }
    }

    public void reset() {
        for (OptionGroupSpecification groupSpec : options.values()) {
            for (OptionSpecification spec : groupSpec.getOptions()) {
                spec.reset();
            }
        }
    }

    /**
     * The option will be added to the unnamed group
     * @param option the option to add
     */
    public Options addOption(OptionSpecification option) {
        return addOption(null, option);
    }

    public Options addOptionGroup(OptionGroupSpecification group) {
        options.put(group.getName(), group);
        return this;
    }

    public Options addOption(String group, OptionSpecification option) {
        OptionGroupSpecification optGroup = options.get(group);

        if (optGroup == null) {
            optGroup = new OptionGroupSpecification(group);
            optGroup.addOption(option);
            options.put(group, optGroup);
        } else {
            optGroup.addOption(option);
        }
        return this;
    }

    public OptionGroupSpecification getGroup(String name) {
        return options.get(name);
    }

    public boolean contains(OptionSpecification option) {
        for (OptionGroupSpecification group : options.values()) {
            if (group.contains(option)) {
                return true;
            }
        }

        return false;
    }

    public LinkedHashMap<String, OptionGroupSpecification> getOptions() {
        return options;
    }

    public OptionSpecification getOption(String name) {
        for (OptionGroupSpecification group : options.values()) {
            OptionSpecification opt = group.getOption(name);

            if (opt != null) {
                return opt;
            }
        }

        return null;
    }

    public int nOptions() {
        int size = 0;
        for (OptionGroupSpecification g : options.values()) {
            size += g.nOptions();
        }
        return size;
    }

    public boolean isEmpty() {
        return nOptions() == 0;
    }

    @Override
    public Iterator<OptionSpecification> iterator() {
        return new Iterator<>() {
            private final Iterator<OptionGroupSpecification> groupIterator = options
                    .values()
                    .iterator();

            private Iterator<OptionSpecification> optionIterator = null;

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
            public OptionSpecification next() {
                OptionSpecification opt = optionIterator.next();

                if (!optionIterator.hasNext()) {
                    optionIterator = null;
                }

                return opt;
            }
        };
    }
}
