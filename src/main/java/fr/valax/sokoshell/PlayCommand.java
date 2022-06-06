package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.MutableMap;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.MapRenderer;
import fr.valax.sokoshell.utils.View;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp;

import java.util.List;

public class PlayCommand extends AbstractVoidCommand {

    @Option(names = {"p", "-pack"}, hasArgument = true, argName = "Pack name", optional = false)
    private String name;

    @Option(names = {"i", "-index"}, hasArgument = true, argName = "Level index", optional = false)
    private int index;

    public void run() {
        Pack pack = helper.getPack(name);

        if (pack == null) {
            System.out.printf("No pack named %s exists%n", name);
            return;
        }

        index--;
        if (index < 0 || index >= pack.levels().size()) {
            System.out.println("Index out of bounds");
            return;
        }

        Level l = pack.levels().get(index);
        PlayCommand.GameController controller = new PlayCommand.GameController(l);

        try (PlayCommand.PlayView view = new PlayCommand.PlayView(helper.getTerminal(), controller)) {
            view.loop();
        }
    }

    @Override
    public String getName() { return "play"; }

    @Override
    public String getUsage() { return "Allows you to play the Sokoban game"; }

    private enum Key {

        ESCAPE,
        LEFT,
        RIGHT,
        DOWN,
        UP,
        ENTER
    }

    public class PlayView extends View<Key> {

        private GameController controller;

        public PlayView(Terminal terminal, GameController controller) {
            super(terminal);
            this.controller = controller;
        }

        @Override
        protected void init() {
            keyMap.bind(PlayCommand.Key.LEFT, KeyMap.key(terminal, InfoCmp.Capability.key_left));
            keyMap.bind(PlayCommand.Key.RIGHT, KeyMap.key(terminal, InfoCmp.Capability.key_right));
            keyMap.bind(PlayCommand.Key.DOWN, KeyMap.key(terminal, InfoCmp.Capability.key_down));
            keyMap.bind(PlayCommand.Key.UP, KeyMap.key(terminal, InfoCmp.Capability.key_up));
            keyMap.bind(PlayCommand.Key.ENTER, "\r");
            keyMap.bind(PlayCommand.Key.ESCAPE, KeyMap.esc());
            keyMap.setAmbiguousTimeout(100L);
        }

        @Override
        protected void render(Size size) {
            List<AttributedString> draw = render();

            int cursorX = draw.get(draw.size() - 1).columnLength();
            int cursorY = draw.size() - 1;

            display.update(draw, size.cursorPos(cursorY, cursorX));
        }

        private List<AttributedString> render() {
            MapRenderer renderer = helper.getRenderer();
            List<AttributedString> draw = renderer.draw(controller.getMap(),
                    controller.getPlayerX(),
                    controller.getPlayerY());

            return draw;
        }

        @Override
        protected void update() {
            if (pressed(PlayCommand.Key.ESCAPE) || pressed(PlayCommand.Key.ENTER)) {
                running = false;
            } else if (pressed(PlayCommand.Key.LEFT)) {


            } else if (pressed(PlayCommand.Key.RIGHT)) {

            } else if (pressed(PlayCommand.Key.UP)) {

            } else if (pressed(PlayCommand.Key.DOWN)) {

            }
        }
    }

    public class GameController {
        MutableMap map;
        private int playerX;
        private int playerY;

        GameController(Level level) {
            this.map = new MutableMap(level.getMap());
            this.playerX = level.getPlayerX();
            this.playerY = level.getPlayerY();
        }

        public MutableMap getMap() { return map; }

        public int getPlayerX() { return playerX; }
        public int getPlayerY() { return playerY; }
    }
}
