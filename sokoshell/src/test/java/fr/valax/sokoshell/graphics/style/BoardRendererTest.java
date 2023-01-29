package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.GraphicsUtils;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.board.Direction;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class BoardRendererTest {

    @Test
    void draw() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"));

        BoardStyle style = BasicStyle.DEFAULT_STYLE;

        Surface s = new Surface();
        s.resize(level.getWidth(), level.getHeight());

        style.draw(new Graphics(s), 1, level, level.getPlayerX(), level.getPlayerY(), Direction.DOWN);

        s.print();
    }


    @Test
    void print() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"));
        BoardStyle style = BasicStyle.DEFAULT_STYLE;
        style.print(level);
    }

    @Test
    void drawWithLegend() {
        Level level = TestUtils.getLevel("""
                ######
                #. @ #
                ###$ #
                ##*  #
                ######
                """);

        BoardStyle style = BasicStyle.DEFAULT_STYLE;

        Surface s = new Surface();
        s.resize(level.getWidth() + 1, level.getHeight() + 1);

        style.drawWithLegend(new Graphics(s), 1, level, level.getPlayerX(), level.getPlayerY(), Direction.DOWN);

        s.print();
    }

    @Test
    void drawWithLegend2() {
        Level level = TestUtils.getLevel("""
                ######
                #. @ #
                ###$ #
                ##*  #
                ######
                """);

        BoardStyle style = BasicStyle.DEFAULT_STYLE;

        Surface s = new Surface();
        s.resize(level.getWidth() * 3 + 1, level.getHeight() * 3 + 1);

        style.drawWithLegend(new Graphics(s), 3, level, level.getPlayerX(), level.getPlayerY(), Direction.DOWN);

        s.print();
    }

    @Test
    void drawWithLegend3() {
        Level level = TestUtils.getLevel("""
                #################
                #. @           #
                ###$  #        #
                ##*            #
                ##      ##     #
                ##   #         #
                ##      #      #
                ##   #*********#
                ################
                """);

        BoardStyle style = BasicStyle.DEFAULT_STYLE;

        Surface s = new Surface();
        s.resize(level.getWidth() + 1, level.getHeight() + 2);

        style.drawWithLegend(new Graphics(s), 1, level, level.getPlayerX(), level.getPlayerY(), Direction.DOWN);

        s.print();
    }

    @Test
    void drawWithLegend4() {
        Level level = TestUtils.getLevel("""
                ################
                #. @           #
                ###$  #        #
                ##*            #
                ##      ##     #
                ##   #         #
                ##      #      #
                ##   #*********#
                ################
                """);

        BoardStyle style = BasicStyle.DEFAULT_STYLE;

        Surface s = new Surface();
        s.resize(level.getWidth() * 3 + 1, level.getHeight() * 3 + 1);

        style.drawWithLegend(new Graphics(s), 3, level, level.getPlayerX(), level.getPlayerY(), Direction.DOWN);

        s.print();
    }

    @Test
    void createImage() throws IOException {
        Level level = TestUtils.getLevel("""
                ################
                #. @           #
                ###$  #        #
                ##*            #
                ##      ##     #
                ##   #         #
                ##      #      #
                ##   #*********#
                ################
                """);

        BoardStyle style = BasicStyle.DEFAULT_STYLE;
        BufferedImage img = style.createImage(level, level.getPlayerX(), level.getPlayerY(), Direction.DOWN);
        ImageIO.write(img, "png", new File("out.png"));
    }

    @Test
    void createImageFileBoardStyle() throws IOException {
        Level level = TestUtils.getLevel("""
                ################
                #. @           #
                ###$  #        #
                ##*            #
                ##      ##     #
                ##   #         #
                ##      #      #
                ##   #*********#
                ################
                """);

        BoardStyle style = TestUtils.getStyle(Path.of("warehouse/warehouse.style"));
        BufferedImage img = style.createImage(level, level.getPlayerX(), level.getPlayerY(), Direction.DOWN);
        ImageIO.write(img, "png", new File("out2.png"));
    }

    @Test
    void createImageWithLegend() throws IOException {
        Level level = TestUtils.getLevel("""
                ################
                #. @           #
                ###$  #        #
                ##*            #
                ##      ##     #
                ##   #         #
                ##      #      #
                ##   #*********#
                ################
                """);

        BoardStyle style = BasicStyle.DEFAULT_STYLE;
        BufferedImage img = style.createImageWithLegend(level, level.getPlayerX(), level.getPlayerY(), Direction.DOWN);
        ImageIO.write(img, "png", new File("out.png"));
    }

    @Test
    void createImageFileBoardStyleWithLegend() throws IOException {
        Level level = TestUtils.getLevel("""
                ################
                #. @           #
                ###$  #        #
                ##*            #
                ##      ##     #
                ##   #         #
                ##      #      #
                ##   #*********#
                ################
                """);

        BoardStyle style = TestUtils.getStyle(Path.of("warehouse/warehouse.style"));
        BufferedImage img = style.createImageWithLegend(level, level.getPlayerX(), level.getPlayerY(), Direction.DOWN);
        ImageIO.write(img, "png", new File("out2.png"));
    }
}
