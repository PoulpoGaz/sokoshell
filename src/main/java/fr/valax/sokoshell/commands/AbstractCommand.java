package fr.valax.sokoshell.commands;

import fr.valax.args.jline.JLineCommand;
import fr.valax.interval.Set;
import fr.valax.interval.*;
import fr.valax.sokoshell.SokoShellHelper;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.GlobIterator;
import fr.valax.sokoshell.utils.Iterators;
import fr.valax.sokoshell.utils.TriFunction;
import fr.valax.sokoshell.utils.Utils;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public abstract class AbstractCommand implements JLineCommand {

    public static AbstractCommand newCommand(TriFunction<InputStream, PrintStream, PrintStream, Integer> function,
                                             String name,
                                             String usage) {
        return newCommand(function, name, usage, true);
    }

    public static AbstractCommand newCommand(TriFunction<InputStream, PrintStream, PrintStream, Integer> function,
                                             String name,
                                             String usage,
                                             boolean addHelp) {
        if (usage == null) {
            return newCommand(function, name, null, new String[0], addHelp);
        } else {
            String[] parts = usage.split("[\n\r]");

            if (parts.length == 1) {
                return newCommand(function, name, parts[0], new String[0], addHelp);
            } else {
                return newCommand(function, name, parts[0], Arrays.copyOfRange(parts, 1, parts.length), addHelp);
            }
        }
    }

    public static AbstractCommand newCommand(TriFunction<InputStream, PrintStream, PrintStream, Integer> function,
                                             String name,
                                             String shortDescription,
                                             String[] usage,
                                             boolean addHelp) {
        return new AbstractCommand() {
            @Override
            protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
                return function.apply(in, out, err);
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getShortDescription() {
                return shortDescription;
            }

            @Override
            public String[] getUsage() {
                return usage;
            }

            @Override
            public boolean addHelp() {
                return addHelp;
            }
        };
    }


    protected final SokoShellHelper helper = SokoShellHelper.INSTANCE;
    protected final SetParser parser = new SetParser();

    @Override
    public int execute(InputStream in, PrintStream out, PrintStream err) {
        helper.lock();

        try {
            return executeImpl(in, out, err);
        } finally {
            helper.unlock();
        }
    }

    protected abstract int executeImpl(InputStream in, PrintStream out, PrintStream err);


    /**
     * @param name the name of the pack
     * @return the pack matching name or the selected pack if name is null
     * @throws InvalidArgument if name is null and there is no selected pack
     * or there is no pack with the specified name
     */
    protected Pack getPack(String name) throws InvalidArgument {
        if (name == null) {
            Pack selected = helper.getSelectedPack();

            if (selected == null) {
                throw new InvalidArgument("No pack selected");
            }

            return selected;
        } else {
            Pack pack = helper.getPack(name);

            if (pack == null) {
                throw new InvalidArgument("No pack named" + name +  " exists");
            }

            return pack;
        }
    }

    /**
     * @param glob glob
     * @return packs that match globs or the selected pack if null
     * @see GlobIterator
     */
    protected Collection<Pack> getPackMultiple(String... glob) {
        if (deepNull(glob)) {
            Pack selected = helper.getSelectedPack();

            if (selected == null) {
                return helper.getPacks();
            }

            return List.of(selected);
        } else {
            java.util.Set<Pack> packs = new HashSet<>();

            for (String g : glob) {
                if (g == null) {
                    Pack selected = helper.getSelectedPack();

                    if (selected != null) {
                        packs.add(selected);
                    }

                } else {
                    GlobIterator<Pack> it = new GlobIterator<>(g, helper.getPacks(), Pack::name);

                    while (it.hasNext()) {
                        packs.add(it.next());
                    }
                }
            }

            return packs;
        }
    }

    protected boolean deepNull(String[] array) {
        if (array != null) {
            for (String str : array) {
                if (str != null) {
                    return false;
                }
            }

        }
        return true;
    }

    /**
     *
     * @param pack the pac to get the level
     * @param index the index of the level or null to get the selected level
     * @return the index-th level of the pack or return the level of the pack with selected index
     * @throws InvalidArgument if index is not an int or if index is out of bounds
     */
    protected Level getLevel(Pack pack, String index) throws InvalidArgument {
        if (index == null) {
            return getLevel(pack, (Integer) null);
        } else {
            OptionalInt optI = Utils.parseInt(index);

            if (optI.isPresent()) {
                return getLevel(pack, optI.getAsInt());
            } else {
                throw new InvalidArgument("Not an integer");
            }
        }
    }

    /**
     *
     * @param pack the pac to get the level
     * @param index the index of the level or null to get the selected level
     * @return the index-th level of the pack or return the level of the pack with selected index
     * @throws InvalidArgument if index is out of bounds
     */
    protected Level getLevel(Pack pack, Integer index) throws InvalidArgument {
        if (index == null) {
            Pack selectedPack = helper.getSelectedPack();
            int i = helper.getSelectedLevelIndex();

            if (i <= 0 || i >= pack.nLevel()) {
                if (selectedPack != null) {
                    if (i <= 0 || selectedPack.nLevel() >= i) {
                        throw new InvalidArgument("No level selected");
                    }
                }

                throw new InvalidArgument("Index out of bounds");
            } else {
                return pack.levels().get(i);
            }
        } else {
            int i = index - 1;

            if (i < 0 || i >= pack.levels().size()) {
                throw new InvalidArgument("Index out of bounds");
            }

            return pack.levels().get(i);
        }
    }

    /**
     * @param pack pack
     * @param range range
     * @return all levels in the pack that match the range. If range is null, it returns all levels
     * @throws InvalidArgument if the range can't be parsed
     */
    protected List<Level> getLevelMultiple(Pack pack, String range) throws InvalidArgument {
        if (range == null || range.isEmpty()) {
            return pack.levels();
        }

        Set set;
        try {
            set = parser.parse(range);
        } catch (ParseException e) {
            throw new InvalidArgument(e);
        }

        return getLevelMultiple(pack, set);
    }

    /**
     *
     * @param packs packs to fetch levels
     * @param range range
     * @return all levels in the collection that are in the range
     * @throws InvalidArgument if the range can't be parsed
     */
    protected List<Level> getLevelMultiple(Collection<Pack> packs, String range) throws InvalidArgument {
        List<Level> levels = new ArrayList<>();

        Set set;
        if (range == null || range.isEmpty()) {
            set = Interval.all();
        } else {
            try {
                set = parser.parse(range);
            } catch (ParseException e) {
                throw new InvalidArgument(e);
            }
        }

        for (Pack pack : packs) {
            levels.addAll(getLevelMultiple(pack, set));
        }

        return levels;
    }

    /**
     * @param pack pack
     * @param set set
     * @return all levels of the pack that are in the set
     */
    protected List<Level> getLevelMultiple(Pack pack, Set set) {
        return Iterators.iteratorToList(getLevelMultipleIt(pack, set));
    }

    /**
     *
     * @param pack pack
     * @param set set
     * @return an iterator of all levels of the pack that are in the set
     */
    protected Iterator<Level> getLevelMultipleIt(Pack pack, Set set) {
        List<Level> levels = pack.levels();
        if (set instanceof Singleton s) {
            int v = (int) s.value() - 1;

            if (v >= 0 && v < levels.size()) {
                return Iterators.singleValueIterator(levels.get(v));
            } else {
                return Iterators.emptyIterator();
            }
        } else if (set instanceof Interval i && containsAllLevels(i, levels.size())) {
            return levels.iterator();
        } else {
            return levels.stream()
                    .filter((l) -> set.contains(l.getIndex() + 1))
                    .iterator();
        }
    }

    private boolean containsAllLevels(Interval i, int nLevel) {
        boolean inf = i.inf() < 0 || (i.inf() == 0 && i.getLeftBound().isClosed());
        boolean sup = i.sup() > nLevel || (i.sup() == nLevel && i.getRightBound().isClosed());

        return inf && sup;
    }

    protected static class InvalidArgument extends Exception {

        public InvalidArgument(String message) {
            super(message);
        }

        public InvalidArgument(Throwable cause) {
            super(cause);
        }

        public void print(PrintStream err, boolean full) {
            Throwable cause = getCause();

            if (cause != null) {
                print(err, cause, full);
            } else {
                print(err, this, full);
            }
        }

        private void print(PrintStream err, Throwable throwable, boolean full) {
            if (full) {
                throwable.printStackTrace(err);
            } else {
                err.println(throwable);
            }
        }
    }
}
