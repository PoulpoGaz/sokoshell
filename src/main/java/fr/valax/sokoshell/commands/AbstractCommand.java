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

    protected List<Pack> getPackMultiple(String... glob) {
        if (glob == null) {
            Pack selected = helper.getSelectedPack();

            if (selected == null) {
                return List.of();
            }

            return List.of(selected);
        } else {
            List<Pack> packs = new ArrayList<>();

            for (String g : glob) {
                GlobIterator<Pack> it = new GlobIterator<>(g, helper.getPacks(), Pack::name);

                while (it.hasNext()) {
                    packs.add(it.next());
                }
            }

            return packs;
        }
    }

    protected Level getLevel(Pack pack, String index) throws InvalidArgument {
        if (index == null) {
            Level selected = helper.getSelectedLevel();

            if (selected == null) {
                throw new InvalidArgument("No level selected");
            }

            return selected;
        } else {
            OptionalInt optI = Utils.parseInt(index);

            if (optI.isPresent()) {
                int i = optI.getAsInt() - 1;

                if (i < 0 || i >= pack.levels().size()) {
                    throw new InvalidArgument("Index out of bounds");
                }

                return pack.levels().get(i);
            }

            throw new InvalidArgument("Not an integer");
        }
    }

    protected Level getLevel(Pack pack, Integer index) throws InvalidArgument {
        if (index == null) {
            Level selected = helper.getSelectedLevel();

            if (selected == null) {
                throw new InvalidArgument("No level selected");
            }

            return selected;
        } else {
            int i = index - 1;

            if (i < 0 || i >= pack.levels().size()) {
                throw new InvalidArgument("Index out of bounds");
            }

            return pack.levels().get(i);
        }
    }

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

    protected List<Level> getLevelMultiple(Pack pack, Set set) {
        return Iterators.iteratorToList(getLevelMultipleIt(pack, set));
    }

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
