package fr.valax.sokoshell;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.readers.PackReaders;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.ImmutableBoard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class LevelOrganizer {

    public static void main(String[] args) {
        List<Pack> packs = loadAllPacks();

        if (packs == null) {
            System.err.println("Failed to load packs");
            return;
        }

        for (int i = 0; i < packs.size(); i++) {
            Pack p = packs.get(i);

            for (int j = i + 1; j < packs.size(); j++) {
                Pack p2 = packs.get(j);

                if (comparePacks(p, p2)) {
                    System.out.println("---------------------------------------------");
                }
            }
        }
    }

    private static boolean comparePacks(Pack p, Pack p2) {
        boolean print = false;
        if (p.name().equals(p2.name())) {
            System.out.println("Duplicate pack name: " + p.getSourcePath() + " and " + p2.getSourcePath());
            print = true;
        }

        Set<Level> duplicates = new HashSet<>();
        for (int i = 0; i < p.nLevels(); i++) {
            Level level1 = p.getLevel(i);

            for (int j = 0; j < p2.nLevels(); j++) {
                Level level2 = p2.getLevel(j);

                if (equals(level1, level2)) {
                    duplicates.add(level1);
                }
            }
        }

        if (duplicates.size() == p.nLevels() && duplicates.size() == p2.nLevels()) {
            System.out.println("Fully duplicated pack: " + p.getSourcePath() + " and " + p2.getSourcePath());
            print = true;

            if (p.getSourcePath().toString().endsWith(".slc")) {
                try {
                    Files.delete(p.getSourcePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (p2.getSourcePath().toString().endsWith(".slc")) {
                try {
                    Files.delete(p2.getSourcePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (duplicates.size() > 0) {
            System.out.println("Packs share some levels: " + p.getSourcePath() + " and " + p2.getSourcePath());
            print = true;
        }

        return print;
    }

    private static boolean equals(Level level1, Level level2) {
        if (level1.getWidth() != level2.getWidth() ||
                level1.getHeight() != level2.getHeight() ||
                level1.getPlayerX() != level2.getPlayerX() ||
                level1.getPlayerY() != level2.getPlayerY()) {
            return false;
        }

        Board board1 = level1.getBoard();
        Board board2 = level2.getBoard();
        for (int y = 0; y < board1.getHeight(); y++) {
            for (int x = 0; x < board1.getWidth(); x++) {
                if (board1.getAt(x, y).getTile() != board2.getAt(x, y).getTile()) {
                    return false;
                }
            }
        }

        return true;
    }


    private static List<Pack> loadAllPacks() {
        System.out.println("Loading packs...");

        try (Stream<Path> paths = Files.walk(Path.of("levels"))) {
            List<Pack> packs = new ArrayList<>();
            Iterator<Path> it = paths.iterator();

            while (it.hasNext()) {
                Path path = it.next();

                if (Files.isDirectory(path)) {
                    continue;
                }

                System.out.println("Loading " + path);
                packs.add(PackReaders.read(path, false));
            }

            return packs;
        } catch (IOException | JsonException e) {
            e.printStackTrace();
            return null;
        }
    }
}
