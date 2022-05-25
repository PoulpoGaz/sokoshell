package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Tile;

import java.util.List;

/**
 * @author PoulpoGaz
 */
public class PrintCommand extends AbstractVoidCommand {

    public PrintCommand(SokoShellHelper helper) {
        super(helper);
    }

    @Option(names = {"p", "-pack"}, argName = "Pack name")
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

    private void printMap(Level level) {
        Tile[][] content = level.getMap().getContent();

        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {
                boolean player = x == level.getPlayerX() && y == level.getPlayerY();

                switch (content[y][x]) {
                    case WALL -> System.out.print('#');
                    case FLOOR -> {
                        if (player) {
                            System.out.print('@');
                        } else {
                            System.out.print(' ');
                        }
                    }
                    case TARGET -> {
                        if (player) {
                            System.out.print('.');
                        } else {
                            System.out.print('+');
                        }
                    }
                    case CRATE -> System.out.print('@');
                    case CRATE_ON_TARGET -> System.out.print('*');
                }

            }
            System.out.println();
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

    @Override
    public boolean addHelp() {
        return true;
    }
}
