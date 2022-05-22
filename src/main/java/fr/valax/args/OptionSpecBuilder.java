package fr.valax.args;

import java.util.ArrayList;
import java.util.List;

public class OptionSpecBuilder {

    private final List<String> names = new ArrayList<>();
    private String description = null;
    private boolean optional = true;
    private boolean allowDuplicate = true;
    private String defaultValue = null;
    private String argName = null;


    public OptionSpec build() {
        if (names.size() == 0) {
            throw new IllegalStateException("An option should have a name");
        }

        return new OptionSpec(names.toArray(new String[0]),
                description, optional,
                allowDuplicate, defaultValue,
                argName);
    }

    public List<String> names() {
        return names;
    }

    public OptionSpecBuilder name(String name) {
        names.add(name);
        return this;
    }

    public String desc() {
        return description;
    }

    public OptionSpecBuilder desc(String description) {
        this.description = description;
        return this;
    }

    public boolean optional() {
        return optional;
    }

    public OptionSpecBuilder optional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public boolean allowDuplicate() {
        return allowDuplicate;
    }

    public OptionSpecBuilder allowDuplicate(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
        return this;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public OptionSpecBuilder defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String argName() {
        return argName;
    }

    public OptionSpecBuilder argName(String argName) {
        this.argName = argName;
        return this;
    }
}
