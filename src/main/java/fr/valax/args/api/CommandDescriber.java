package fr.valax.args.api;

import java.util.List;
import java.util.Map;

public interface CommandDescriber {

    String getName();

    String getUsage();

    boolean hasDefaultHelp();

    OptionIterator optionIterator();

    Option getOption(String name);

    int nOptions();

    Map<OptionGroup, List<Option>> getOptions();
}
