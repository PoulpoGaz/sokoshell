package fr.valax.args;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;

import java.util.*;

/**
 * An internal object used to describe an option.
 * It also contains {@link #arguments} and {@link #present} fields.
 * They serve to the parser: to store arguments before converting and
 * checking if an option is required but not present
 */
public class OptionSpecification {

    private final String[] names;
    private final String description;
    private final boolean optional;
    private final boolean allowDuplicate;
    private final String defaultValue;
    private final String argumentName;

    private final List<String> arguments = new ArrayList<>();
    private boolean present;

    public OptionSpecification(Option option) {
        names = option.names();
        description = option.description();
        optional = option.optional();
        allowDuplicate = option.allowDuplicate();
        defaultValue = option.defaultValue();
        argumentName = option.argName();
    }

    public OptionSpecification(String[] names,
                               String description,
                               boolean optional,
                               boolean allowDuplicate,
                               String defaultValue,
                               String argName) {
        this.names = Objects.requireNonNull(names);
        this.description = description;
        this.optional = optional;
        this.allowDuplicate = allowDuplicate;
        this.defaultValue = defaultValue;
        this.argumentName = argName;
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

    public boolean hasName(String name) {
        return ArgsUtils.contains(names, name);
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
        return argumentName != null && !argumentName.isEmpty();
    }

    public List<String> getArgumentsList() {
        return arguments;
    }

    public Optional<String> getArgument(int index) {
        if (index < 0 || index >= arguments.size()) {
            return Optional.empty();
        } else {
            return Optional.of(arguments.get(index));
        }
    }

    public boolean isPresent() {
        return present;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OptionSpecification that = (OptionSpecification) o;

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

    @Override
    public String toString() {
        return "OptionSpec{" +
                "names=" + Arrays.toString(names) +
                ", description='" + description + '\'' +
                ", optional=" + optional +
                ", allowDuplicate=" + allowDuplicate +
                ", defaultValue='" + defaultValue + '\'' +
                ", argumentName='" + argumentName + '\'' +
                ", arguments=" + arguments +
                ", present=" + present +
                '}';
    }
}