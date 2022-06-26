package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.Level;
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
        Level l = getLevel();

        if (l == null) {
            return FAILURE;
        }

        Solution solution = l.getSolution();

        if (solution == null) {
            System.out.println("Not solved");
            return FAILURE;
        }

        BufferedImage image = solution.createGraph();

        String file = l.getIndex() + " - " + solution.getType() + ".png";
        try {
            File outFile = new File(file);

            ImageIO.write(image, "png", new File(file));
            System.out.println("Image written at " + outFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save image");

            return FAILURE;
        }

        return SUCCESS;
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
