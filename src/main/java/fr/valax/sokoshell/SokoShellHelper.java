package fr.valax.sokoshell;

import fr.poulpogaz.json.JsonException;
import fr.valax.args.CommandLine;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Solver;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.terminal.Terminal;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Contains all packs, styles, the task list, the selected level, the selected pack and the map renderer.
 * All commands will interact with this object to gather data
 */
public class SokoShellHelper {

    private static final Path exportFolder = Path.of("export");

    public static final SokoShellHelper INSTANCE = new SokoShellHelper();

    private final Map<String, Pack> packs = new HashMap<>();
    private final Map<String, MapStyle> styles = new HashMap<>();

    private final MapRenderer renderer = new MapRenderer();
    private final PNGExporter exporter = new PNGExporter();

    private CommandLine cli;
    private Terminal terminal;

    private final TaskList taskList;

    private Pack selectedPack = null;
    private int selectedLevel = -1;

    private boolean autoSaveSolution = false;

    private SokoShellHelper() {
        addMapStyle(MapStyle.DEFAULT_STYLE);
        renderer.setStyle(MapStyle.DEFAULT_STYLE);
        exporter.setMapStyle(MapStyle.DEFAULT_STYLE);

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

    // export

    public void exportPNG(Level level, int size) throws IOException {
        exportPNG(level.getPack(), level, level.getMap(), level.getPlayerX(), level.getPlayerY(), Direction.DOWN, size);
    }

    public void exportPNG(Pack pack, Level level, fr.valax.sokoshell.solver.Map map,
                          int playerX, int playerY, Direction playerDir)
            throws IOException {
        exportPNG(pack, level, map, playerX, playerY, playerDir, 16);
    }

    public void exportPNG(Pack pack, Level level, fr.valax.sokoshell.solver.Map map,
                          int playerX, int playerY, Direction playerDir, int size)
            throws IOException {
        BufferedImage image = exporter.asImage(map, playerX, playerY, playerDir, size);

        Path out;
        if (pack == null && level == null) {
            out = exportFolder.resolve("level.png");
        } else if (pack != null && level == null) {
            out = exportFolder.resolve(pack.name() + ".png");
        } else if (pack == null) {
            out = exportFolder.resolve(level.getIndex() + ".png");
        } else {
            out = exportFolder.resolve(pack.name() + "_" + level.getIndex() + ".png");
        }

        out = Utils.checkExists(out);

        Path parent = out.getParent();
        if (pack != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        ImageIO.write(image, "png", out.toFile());
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

        if (renderer.getStyle().getName().equals(mapStyle.getName())) {
            renderer.setStyle(mapStyle);
            exporter.setMapStyle(mapStyle);
        }
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
        exporter.setMapStyle(style);
    }

    public Collection<MapStyle> getMapStyles() {
        return styles.values();
    }


    public MapRenderer getRenderer() {
        return renderer;
    }

    public PNGExporter getExporter() {
        return exporter;
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
