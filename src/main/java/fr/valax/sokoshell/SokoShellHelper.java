package fr.valax.sokoshell;

import fr.poulpogaz.json.JsonException;
import fr.valax.args.CommandLine;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Solver;
import org.jline.reader.Candidate;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.util.*;

/**
 * Contains all packs, styles, the task list, the selected level, the selected pack and the map renderer.
 * All commands will interact with this object to gather data
 */
public class SokoShellHelper {

    public static final SokoShellHelper INSTANCE = new SokoShellHelper();

    private final Map<String, Pack> packs = new HashMap<>();
    private final Map<String, MapStyle> styles = new HashMap<>();

    private final MapRenderer renderer = new MapRenderer();

    private CommandLine cli;
    private Terminal terminal;

    private final TaskList taskList;

    private Pack selectedPack = null;
    private int selectedLevel = -1;

    private boolean autoSaveSolution = false;

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

    /**
     * Add a pack. If there were a pack with the same name, it is removed
     * @param pack the pack to add
     */
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
        if (selectedPack == null || selectedLevel < 0 || selectedLevel >= selectedPack.nLevel()) {
            return null;
        } else {
            return selectedPack.getLevel(selectedLevel);
        }
    }

    /**
     * @return -1 if there is no selected level otherwise the index of the selected level
     */
    public int getSelectedLevelIndex() {
        if (selectedPack == null || selectedLevel < 0 || selectedLevel >= selectedPack.nLevel()) {
            return -1;
        } else {
            return selectedLevel;
        }
    }

    // auto save

    public boolean isAutoSaveSolution() {
        return autoSaveSolution;
    }

    public void setAutoSaveSolution(boolean autoSaveSolution) {
        this.autoSaveSolution = autoSaveSolution;
    }

    public void saveAllSolution() throws IOException, JsonException {
        for (Pack pack : packs.values()) {
            pack.writeSolutions(null);
        }
    }

    // styles

    /**
     * Add the map style if there is no map style with his name
     * @param mapStyle the map style to add
     * @return true if it was added
     */
    public boolean addMapStyle(MapStyle mapStyle) {
        if (styles.containsKey(mapStyle.getName())) {
            return false;
        } else {
            styles.put(mapStyle.getName(), mapStyle);

            return true;
        }
    }

    /**
     * Add a map style. If there were a map style with the same name, it is removed
     * @param mapStyle the map style to add
     */
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
}
