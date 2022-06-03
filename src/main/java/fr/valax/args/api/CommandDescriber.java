package fr.valax.args.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface CommandDescriber extends Iterable<Option> {

    Command<?> getCommand();

    String getName();

    String getUsage();

    boolean hasDefaultHelp();

    OptionIterator optionIterator();

    Option getOption(String name);

    int nOptions();

    Map<OptionGroup, List<Option>> getOptions();

    @Override
    default Iterator<Option> iterator() {
        return optionIterator();
    }
}
