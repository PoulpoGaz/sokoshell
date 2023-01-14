package fr.valax.sokoshell.readers;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Tile;
import fr.valax.sokoshell.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a file and try to find level in the format .xsb
 */
public class XSBReader implements Reader {

    private static final String SYMBOLS = "#@+$*. -_";

    @Override
    public Pack read(Path path, boolean loadSolution) throws IOException, JsonException {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            List<Level> levels = readLevels(br);

            Pack p = new Pack(Utils.getFileName(path), null, levels);
            p.setSourcePath(path);

            if (loadSolution) {
                Path solutionPath = Path.of(path + ".solutions.json.gz");
                p.readSolutions(solutionPath);
            }

            return p;
        }
    }

    @Override
    public Pack read(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        List<Level> levels = readLevels(br);

        return new Pack(null, null, levels);
    }

    private List<Level> readLevels(BufferedReader br) throws IOException {
        List<Level> levels = new ArrayList<>();

        int i = 0;
        String line;
        while ((line = br.readLine()) != null) {
            if (isSokobanLine(line)) {
                Level level = parseLevel(line, i, br);

                if (level != null) {
                    levels.add(level);

                    i++;
                }
            }
        }

        return levels;
    }

    private static boolean isSokobanLine(String line) {
        if (line == null || line.length() == 0) {
            return false;
        }

        for (char c : line.toCharArray()) {
            if (SYMBOLS.indexOf(c) < 0) {
                return false;
            }
        }

        return true;
    }

    private static Level parseLevel(String firstLine, int index, BufferedReader br) throws IOException {
        int width = firstLine.length();
        int height;

        List<String> lines = new ArrayList<>();
        lines.add(firstLine);

        String line;
        while ((line = br.readLine()) != null) {
            if (isSokobanLine(line)) {
                width = Math.max(width, line.length());

                lines.add(line);
            } else {
                break;
            }
        }

        height = lines.size();

        if (width < Map.MINIMUM_WIDTH && height < Map.MINIMUM_HEIGHT) {
            return null;
        }

        Level.Builder builder = new Level.Builder();
        builder.setSize(width, height);

        int y = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);

            int x = 0;
            for (char c : line.toCharArray()) {
                if (!ReaderUtils.set(c, builder, x, y)) {
                    return null;
                }

                x++;
            }

            if (x != width) {
                for (; x < width; x++) {
                    builder.set(Tile.FLOOR, x, y);
                }
            }

            y++;
        }

        builder.setIndex(index);
        return builder.build();
    }
}
