package fr.valax.args;

import java.util.ArrayList;
import java.util.List;

public class OptionSpecificationBuilder {

    private final List<String> names = new ArrayList<>();
    private String description = null;
    private boolean optional = true;
    private boolean allowDuplicate = true;
    private String defaultValue = null;
    private String argName = null;


    public OptionSpecification build() {
        if (names.size() == 0) {
            throw new IllegalStateException("An option should have a name");
        }

        return new OptionSpecification(names.toArray(new String[0]),
                description, optional,
                allowDuplicate, defaultValue,
                argName);
    }

    public List<String> names() {
        return names;
    }

    public OptionSpecificationBuilder name(String name) {
        names.add(name);
        return this;
    }

    public String desc() {
        return description;
    }

    public OptionSpecificationBuilder desc(String description) {
        this.description = description;
        return this;
    }

    public boolean optional() {
        return optional;
    }

    public OptionSpecificationBuilder optional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public boolean allowDuplicate() {
        return allowDuplicate;
    }

    public OptionSpecificationBuilder allowDuplicate(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
        return this;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public OptionSpecificationBuilder defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String argName() {
        return argName;
    }

    public OptionSpecificationBuilder argName(String argName) {
        this.argName = argName;
        return this;
    }
}
