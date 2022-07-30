package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * @author PoulpoGaz
 */
public class PrintCommand extends PackCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        List<Pack> packs = getPackMultiple();

        for (Pack pack : packs) {
            out.printf("Pack: %s%n", pack.name());

            List<Level> levels = pack.levels();

            for (int i = 0; i < levels.size(); i++) {
                Level l = levels.get(i);

                out.printf("<===== Level n°%d =====>%n", i + 1);
                helper.getRenderer().print(out, l);
            }
        }

        return SUCCESS;
    }

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public String getShortDescription() {
        return "print a state";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
