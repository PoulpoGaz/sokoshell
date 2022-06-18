package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Solution;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class StatsCommand extends LevelCommand {

    @Override
    public void run() {
        Level l = getLevel();

        if (l == null) {
            return;
        }

        Solution solution = l.getSolution();

        if (solution == null) {
            System.out.println("Not solved");
            return;
        }

        BufferedImage image = solution.createGraph();

        String file = l.getIndex() + " - " + solution.getType() + ".png";
        try {
            File out = new File(file);

            ImageIO.write(image, "png", new File(file));
            System.out.println("Image written at " + out.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save image");
        }
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getUsage() {
        return "Print stats about a solution";
    }
}
