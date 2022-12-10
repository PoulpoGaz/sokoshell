package fr.valax.args.api;

import java.util.Iterator;

/**
 * DOESN'T RETURN DEFAULT HELP!!!!
 */
public interface OptionIterator extends Iterator<Option> {

    OptionGroup currentGroup();

    boolean groupChanged();

    boolean hasNextGroup();

    OptionGroup nextGroup();
}
