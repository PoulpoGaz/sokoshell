package fr.valax.args;

import fr.valax.args.api.Option;
import fr.valax.args.api.OptionGroup;
import fr.valax.args.api.OptionIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class OptionIteratorImpl implements OptionIterator {

    private final Iterator<Map.Entry<OptionGroup, List<Option>>> groupIterator;
    private Iterator<Option> optionIterator = null;

    private OptionGroup last = null;
    private OptionGroup current = null;

    public OptionIteratorImpl(Map<OptionGroup, List<Option>> optionsByGroup) {
        this.groupIterator = optionsByGroup.entrySet().iterator();
    }

    @Override
    public OptionGroup currentGroup() {
        return current;
    }

    @Override
    public boolean groupChanged() {
        return last != current;
    }

    @Override
    public boolean hasNextGroup() {
        return groupIterator.hasNext();
    }

    @Override
    public OptionGroup nextGroup() {
        if (!hasNextGroup()) {
            throw new NoSuchElementException();
        }

        Map.Entry<OptionGroup, List<Option>> entry = groupIterator.next();

        last = current;
        current = entry.getKey();
        optionIterator = entry.getValue().iterator();

        return current;
    }

    @Override
    public boolean hasNext() {
        if (optionIterator != null) {
            return optionIterator.hasNext() || groupIterator.hasNext();
        } else {
            return groupIterator.hasNext();
        }
    }

    @Override
    public Option next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        if (optionIterator == null) {
            nextGroup();
        }

        Option next = optionIterator.next();

        if (!optionIterator.hasNext()) {
            optionIterator = null;
        }

        return next;
    }
}