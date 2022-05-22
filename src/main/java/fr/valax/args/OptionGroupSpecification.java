package fr.valax.args;

import fr.valax.args.utils.ArgsUtils;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * An internal object used to describe an OptionGroup.
 * It also contains references to his options
 */
class OptionGroupSpecification implements Iterable<OptionSpecification> {

    private final String name;
    private final LinkedHashSet<OptionSpecification> options;

    public OptionGroupSpecification(String name) {
        this.name = name;
        this.options = new LinkedHashSet<>();
    }

    public OptionGroupSpecification(OptionGroupSpecification... groups) {
        this.name = groups[0].getName();
        this.options = new LinkedHashSet<>();

        for (OptionGroupSpecification group : groups) {
            options.addAll(group.getOptions());
        }
    }

    public OptionSpecification getOption(String name) {
        return ArgsUtils.find(options, (o) -> o.hasName(name));
    }

    public void addOption(OptionSpecification option) {
        options.add(option);
    }

    public LinkedHashSet<OptionSpecification> getOptions() {
        return options;
    }

    public String getName() {
        return name;
    }

    public boolean contains(OptionSpecification option) {
        return options.contains(option);
    }

    public int nOptions() {
        return options.size();
    }

    public boolean isEmpty() {
        return options.isEmpty();
    }

    @Override
    public Iterator<OptionSpecification> iterator() {
        return options.iterator();
    }
}
