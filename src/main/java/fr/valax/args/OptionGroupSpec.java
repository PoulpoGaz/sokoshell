package fr.valax.args;

import fr.valax.args.utils.ArgsUtils;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class OptionGroupSpec implements Iterable<OptionSpec> {

    private final String name;
    private final LinkedHashSet<OptionSpec> options;

    public OptionGroupSpec(String name) {
        this.name = name;
        this.options = new LinkedHashSet<>();
    }

    public OptionGroupSpec(OptionGroupSpec... groups) {
        this.name = groups[0].getName();
        this.options = new LinkedHashSet<>();

        for (OptionGroupSpec group : groups) {
            options.addAll(group.getOptions());
        }
    }

    public OptionSpec getOption(String name) {
        return ArgsUtils.find(options, (o) -> o.hasName(name));
    }

    public void addOption(OptionSpec option) {
        options.add(option);
    }

    public LinkedHashSet<OptionSpec> getOptions() {
        return options;
    }

    public String getName() {
        return name;
    }

    public boolean contains(OptionSpec option) {
        return options.contains(option);
    }

    public int nOptions() {
        return options.size();
    }

    public boolean isEmpty() {
        return options.isEmpty();
    }

    @Override
    public Iterator<OptionSpec> iterator() {
        return options.iterator();
    }
}
