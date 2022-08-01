package fr.valax.sokoshell.commands.level;

import fr.valax.args.api.Command;
import fr.valax.sokoshell.commands.AbstractCommand;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Solution;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class StatsCommand extends LevelCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Level l;
        try {
            Pack pack = getPack(name);
            l = getLevel(pack, index);

        } catch (AbstractCommand.InvalidArgument e) {
            e.print(err, true);
            return Command.FAILURE;
        }

        Solution solution = l.getLastSolution();

        if (solution == null) {
            out.println("Not solved");
            return Command.FAILURE;
        }

        BufferedImage image = solution.createGraph();

        String file = l.getIndex() + " - " + solution.getType() + ".png";
        try {
            File outFile = new File(file);

            ImageIO.write(image, "png", new File(file));
            out.println("Image written at " + outFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace(err);
            out.println("Failed to save image");

            return Command.FAILURE;
        }

        return Command.SUCCESS;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getShortDescription() {
        return "Print stats about a solution";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
