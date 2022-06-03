package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Tile;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.text.AttributedString;
import java.util.List;

/**
 * @author PoulpoGaz
 */
public class PrintCommand extends AbstractVoidCommand {

    public PrintCommand(SokoShellHelper helper) {
        super(helper);
    }

    @Option(names = {"p", "-pack"}, hasArgument = true, argName = "Pack name")
    private String packName;

    @Override
    public void run() {
        if (packName != null && !packName.isBlank()) {
            printPack(packName);
        }
    }

    private void printPack(String name) {
        Pack pack = helper.getPack(name);

        if (pack == null) {
            System.out.println("Can't find a pack named " + name);
            return;
        }

        List<Level> levels = pack.levels();
        for (int i = 0; i < levels.size(); i++) {
            Level l = levels.get(i);

            System.out.printf("<===== Level n°%d =====>%n", i);
            printMap(l);
        }
    }

    public static void printMap(Level level) {
        printMap(level.getMap(), level.getPlayerY() * level.getWidth() + level.getPlayerX());
    }

    public static void printMap(Map map, int playerPos) {
        int playerX = playerPos % map.getWidth();
        int playerY = playerPos / map.getWidth();

        AttributedStringBuilder builder = new AttributedStringBuilder();

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                boolean player = x == playerX && y == playerY;

                switch (map.getAt(x, y)) {
                    case WALL -> builder.styled(AttributedStyle.DEFAULT.background(100, 100, 100), " ");
                    case FLOOR -> {
                        if (player) {
                            builder.styled(AttributedStyle.DEFAULT.background(AttributedStyle.GREEN).foreground(AttributedStyle.BLACK), "o");
                        } else {
                            builder.styled(AttributedStyle.DEFAULT.background(AttributedStyle.GREEN), " ");
                        }
                    }
                    case TARGET -> {
                        if (player) {
                            builder.styled(AttributedStyle.DEFAULT.background(AttributedStyle.RED).foreground(AttributedStyle.BLACK), "o");
                        } else {
                            builder.styled(AttributedStyle.DEFAULT.background(AttributedStyle.RED), " ");
                        }
                    }
                    case CRATE -> builder.styled(AttributedStyle.DEFAULT.background(96, 61, 23), " ");
                    case CRATE_ON_TARGET -> builder.styled(AttributedStyle.DEFAULT.background(AttributedStyle.YELLOW), " ");
                }
            }
            builder.append('\n');
        }

        System.out.println(builder.toAnsi());
    }

    @Override
    public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public String getUsage() {
        return "print a state";
    }
}
