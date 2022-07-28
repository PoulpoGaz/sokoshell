package fr.valax.sokoshell.readers;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Tile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SOKReader implements Reader {

    private static final String SYMBOLS = "#@+$*. -_";

    @Override
    public Pack read(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

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

        br.close();

        return new Pack(null, null, levels);
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

        if (width < Map.MINIMUM_WIDTH || height < Map.MINIMUM_HEIGHT) {
            return null;
        }

        Level.Builder builder = new Level.Builder();
        builder.setSize(width, height);
        builder.setPlayerPos(width - 1, height - 1);

        int y = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);

            int x = 0;
            for (char c : line.toCharArray()) {
                switch (c) {
                    case ' ', '-' -> builder.set(Tile.FLOOR, x, y);
                    case '#', '_' -> builder.set(Tile.WALL, x, y);
                    case '$' -> builder.set(Tile.CRATE, x, y);
                    case '.' -> builder.set(Tile.TARGET, x, y);
                    case '*' -> builder.set(Tile.CRATE_ON_TARGET, x, y);
                    case '@' -> {
                        builder.set(Tile.FLOOR, x, y);
                        builder.setPlayerPos(x, y);
                    }
                    case '+' -> {
                        builder.set(Tile.TARGET, x, y);
                        builder.setPlayerPos(x, y);
                    }
                    default -> {
                        return null;
                    }
                }

                x++;
            }

            if (x != width) {
                for (; x < width; x++) {
                    builder.set(Tile.WALL, x, y);
                }
            }

            y++;
        }

        builder.setIndex(index);
        return builder.build();
    }
}
