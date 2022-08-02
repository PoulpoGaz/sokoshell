package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.solver.*;
import org.jline.reader.Candidate;
import org.jline.terminal.Terminal;

import java.util.Map;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SokoShellHelper implements Lock {

    public static final SokoShellHelper INSTANCE = new SokoShellHelper();

    private final ReentrantLock lock = new ReentrantLock();

    private final Map<String, Pack> packs = new HashMap<>();
    private final Map<String, MapStyle> styles = new HashMap<>();

    private final MapRenderer renderer = new MapRenderer();

    private CommandLine cli;
    private Terminal terminal;

    private final TaskList taskList;

    private Pack selectedPack = null;
    private int selectedLevel = -1;

    private SokoShellHelper() {
        addMapStyle(MapStyle.DEFAULT_STYLE);
        renderer.setStyle(MapStyle.DEFAULT_STYLE);

        taskList = new TaskList();
    }

    // tasks

    public void addTask(Solver solver, Map<String, Object> params, List<Level> levels, String pack, String level) {
        Objects.requireNonNull(params);
        Objects.requireNonNull(levels);

        addTask(new SolverTask(solver, params, levels, pack, level));
    }

    public void addTask(SolverTask task) {
        taskList.offerTask(task);
    }

    public TaskList getTaskList() {
        return taskList;
    }

    // packs

    /**
     * Add a pack if there is no pack with his name
     * @param pack the pack to add
     * @return true if the pack was added
     */
    public boolean addPack(Pack pack) {
        if (packs.containsKey(pack.name())) {
            return false;
        } else {
            packs.put(pack.name(), pack);

            return true;
        }
    }

    public void addPackReplace(Pack pack) {
        packs.put(pack.name(), pack);
    }

    public Pack getPack(String name) {
        return packs.get(name);
    }

    public void addPackCandidates(List<Candidate> candidates) {
        for (Pack pack : getPacks()) {
            candidates.add(new Candidate(pack.name()));
        }
    }

    public void selectPack(Pack pack) {
        this.selectedPack = pack;
    }

    public Pack getSelectedPack() {
        return selectedPack;
    }

    public Collection<Pack> getPacks() {
        return packs.values();
    }

    // levels

    public void selectLevel(int index) {
        this.selectedLevel = index;
    }

    public Level getSelectedLevel() {
        if (selectedPack == null || selectedLevel < 0 || selectedLevel >= selectedPack.levels().size()) {
            return null;
        } else {
            return selectedPack.levels().get(selectedLevel);
        }
    }

    public int getSelectedLevelIndex() {
        return selectedLevel;
    }

    // styles

    public boolean addMapStyle(MapStyle mapStyle) {
        if (styles.containsKey(mapStyle.getName())) {
            return false;
        } else {
            styles.put(mapStyle.getName(), mapStyle);

            return true;
        }
    }

    public void addMapStyleReplace(MapStyle mapStyle) {
        styles.put(mapStyle.getName(), mapStyle);
    }

    public MapStyle getMapStyle(String name) {
        return styles.get(name);
    }

    public void addMapStyleCandidates(List<Candidate> candidates) {
        for (MapStyle mapStyle : getMapStyles()) {
            candidates.add(new Candidate(mapStyle.getName()));
        }
    }

    public MapStyle getMapStyle() {
        return renderer.getStyle();
    }

    public void setMapStyle(MapStyle style) {
        renderer.setStyle(style);
    }

    public Collection<MapStyle> getMapStyles() {
        return styles.values();
    }


    public MapRenderer getRenderer() {
        return renderer;
    }



    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public CommandLine getCli() {
        return cli;
    }

    public void setCli(CommandLine cli) {
        this.cli = cli;
    }



    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock.lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return lock.tryLock(time, unit);
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public Condition newCondition() {
        return lock.newCondition();
    }
}
