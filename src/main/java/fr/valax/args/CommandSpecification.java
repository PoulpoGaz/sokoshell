package fr.valax.args;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.api.OptionGroup;
import fr.valax.args.api.VaArgs;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.valax.args.utils.ArgsUtils.notNull;
import static fr.valax.args.utils.ArgsUtils.thrExc;

/**
 * An internal object to describe a command and
 * for setting fields.
 */
public class CommandSpecification {

    private final Command<?> command;
    private CommandLine cli;

    private Options options;
    private Map<OptionSpecification, Field> optionMapping;
    private Field vaargs;

    public CommandSpecification(Command<?> command) throws CommandLineException {
        this.command = command;

        createOptions();
    }

    protected void createOptions() throws CommandLineException {
        Class<?> class_ = command.getClass();

        options = new Options();
        optionMapping = new HashMap<>();

        for (Field field : getAllFields(class_)) {
            String optGroup = null;
            if (field.isAnnotationPresent(OptionGroup.class)) {
                optGroup = field.getAnnotation(OptionGroup.class).name();
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

                // TODO: rethink about that
                if (option.argName().equals("")) {
                    checkBoolean(field, "%s should be a boolean", field.getName());
                }

                OptionSpecification spec = new OptionSpecification(option);
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
        for (Map.Entry<OptionSpecification, Field> entry : optionMapping.entrySet()) {
            OptionSpecification opt = entry.getKey();
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
        // TODO: doesn't work
        TypeConverter<?> converter = null;
        if (field.isAnnotationPresent(Option.class)) {
            Option option = field.getAnnotation(Option.class);

            if (option.converter().length > 0) {
                converter = cli.getConverter(option.converter()[0]);
            }
        } else if (field.isAnnotationPresent(VaArgs.class)) {
            VaArgs vaArgs = field.getAnnotation(VaArgs.class);

            if (vaArgs.converter().length > 0) {
                converter = cli.getConverter(vaArgs.converter()[0]);
            }
        }

        if (converter == null) {
            Class<?> type = field.getType();
            if (type.isArray()) {
                return cli.getConverter(type.getComponentType());
            } else {
                return cli.getConverter(type);
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
            field.set(command, value);
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
        return command.getName();
    }

    public Command<?> getCommand() {
        return command;
    }

    public Field getVaargs() {
        return vaargs;
    }

    public Options getOptions() {
        return options;
    }


    public void setCli(CommandLine cli) {
        this.cli = cli;
    }
}
