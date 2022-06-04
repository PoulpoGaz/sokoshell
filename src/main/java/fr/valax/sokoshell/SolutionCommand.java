package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.MapRenderer;
import fr.valax.sokoshell.utils.Utils;
import fr.valax.sokoshell.utils.View;
import org.jline.keymap.KeyMap;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.InfoCmp;

import java.util.*;
import java.util.stream.Collectors;

public class SolutionCommand extends AbstractVoidCommand {

    @Option(names = {"p", "-pack"}, hasArgument = true, argName = "Pack name", optional = false)
    private String name;

    @Option(names = {"i", "-index"}, hasArgument = true, argName = "Level index", optional = false)
    private int index;

    @Override
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

        List<Level> levels = pack.levels();
        Level l = levels.get(index);

        if (l.getSolution() == null) {
            System.out.println("Not solved");
            return;
        }

        SolutionAnimator animator = new SolutionAnimator(l);

        try (SolutionView view = new SolutionView(helper.getTerminal(), animator)) {
            view.loop();
        }
    }

    @Override
    public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
        if (ArgsUtils.contains(option.names(), "p")) {
            helper.addPackCandidates(candidates);
        }
    }

    @Override
    public String getName() {
        return "solution";
    }

    @Override
    public String getUsage() {
        return "Show a solution";
    }

    private enum Key {
        ESCAPE,
        LEFT,
        RIGHT,
        DOWN,
        UP,
        ENTER,
        SPACE
    }

    public class SolutionView extends View<Key> {

        private final SolutionAnimator animator;

        private boolean paused = false;

        private long lastTime;

        // a value between 1 and 40
        private int speed = 20;

        public SolutionView(Terminal terminal, SolutionAnimator animator) {
            super(terminal);
            this.animator = animator;
        }

        @Override
        protected void init() {
            keyMap.bind(Key.LEFT, KeyMap.key(terminal, InfoCmp.Capability.key_left));
            keyMap.bind(Key.RIGHT, KeyMap.key(terminal, InfoCmp.Capability.key_right));
            keyMap.bind(Key.DOWN, KeyMap.key(terminal, InfoCmp.Capability.key_down));
            keyMap.bind(Key.UP, KeyMap.key(terminal, InfoCmp.Capability.key_up));
            keyMap.bind(Key.ENTER, "\r");
            keyMap.bind(Key.SPACE, " ");
            keyMap.bind(Key.ESCAPE, KeyMap.esc());
            keyMap.setAmbiguousTimeout(100L);
        }

        @Override
        protected void render(Size size) {
            // reusing the same list doesn't work!!
            List<AttributedString> draw = render(animator);

            int cursorX = draw.get(draw.size() - 1).columnLength();
            int cursorY = draw.size() - 1;

            display.update(draw, size.cursorPos(cursorY, cursorX));
        }

        private List<AttributedString> render(SolutionAnimator animator) {
            MapRenderer renderer = helper.getRenderer();
            List<AttributedString> draw = renderer.draw(animator.getMap(), animator.getPlayerX(), animator.getPlayerY());

            int totalMoveLength = Utils.nDigit(animator.getTotalMove());
            int totalPushLength = Utils.nDigit(animator.getTotalMove());

            int moveLength = Utils.nDigit(animator.getMoveCount());
            int pushLength = Utils.nDigit(animator.getPushCount());

            AttributedStringBuilder builder = new AttributedStringBuilder();
            // moves:  XX/XXX
            builder.append("Moves: ").append(" ".repeat(totalMoveLength - moveLength))
                    .append(String.valueOf(animator.getMoveCount())).append('/')
                    .append(String.valueOf(animator.getTotalMove()));

            builder.append(" | ");

            // pushes: XX/XXX
            builder.append("Pushes: ").append(" ".repeat(totalPushLength - pushLength))
                    .append(String.valueOf(animator.getPushCount())).append('/')
                    .append(String.valueOf(animator.getTotalPush()));

            // speed
            builder.append(" | Speed: ").append(String.valueOf(speed));

            draw.add(builder.toAttributedString());

            if (paused) {
                draw.add(new AttributedString("Paused"));
            }

            draw.add(new AttributedString("FPS: " + getFPS()));
            draw.add(new AttributedString("TPS: " + getTPS()));

            return draw;
        }

        /**
         * f(x) = e^(ax²+bx+c)
         * f(1) = 5000
         * f(20) = 100
         * f(40) = 1000 / View.TPS
         */
        private int speedToMillis() {
            double a = 0.0011166751;
            double b = -0.1920345101;
            double c = 8.708111026;

            return (int) Math.exp(
                a * speed * speed + b * speed + c
            );
        }

        @Override
        protected void update() {
            if (lastTime + speedToMillis() < System.currentTimeMillis()) {
                if (animator.hasNext() && !paused) {
                    animator.move();

                    lastTime = System.currentTimeMillis();

                    if (!animator.hasNext()) {
                        paused = true;
                    }
                }
            }

            if (pressed(Key.ESCAPE)) {
                running = false;
            } else if (pressed(Key.ENTER) && !animator.hasNext()) {
                running = false;
            } else if (pressed(Key.SPACE)) {
                paused = !paused;
                lastTime = 0;
            } else if (pressed(Key.LEFT) && paused) {

                if (animator.hasPrevious()) {
                    animator.moveBackward();
                }

            } else if (pressed(Key.RIGHT) && paused) {

                if (animator.hasNext()) {
                    animator.move();
                }
            } else if (pressed(Key.UP)) {
                if (speed < 40) {
                    speed++;
                }

            } else if (pressed(Key.DOWN)) {
                if (speed > 1) {
                    speed--;
                }
            }
        }
    }

    public static class SolutionAnimator {

        private final MutableMap map;
        private final List<State> states;

        private final List<Move> path;
        private int pathIndex;

        private int playerX;
        private int playerY;

        private int move;
        private int push;

        public SolutionAnimator(Level level) {
            this.states = level.getSolution().getStates();
            this.map = new MutableMap(level.getMap());
            this.playerX = level.getPlayerX();
            this.playerY = level.getPlayerY();

            path = computeFullPath();
        }

        public void move() {
            if (!hasNext()) {
                return;
            }

            Direction dir = path.get(pathIndex).direction();

            playerX += dir.dirX();
            playerY += dir.dirY();
            move++;

            moveCrate(playerX, playerY, dir);

            pathIndex++;
        }

        // move crate if needed
        private void moveCrate(int x, int y, Direction dir) {
            int newX = x + dir.dirX();
            int newY = y + dir.dirY();

            Tile curr = map.getAt(x, y);
            Tile next = map.getAt(newX, newY);

            switch (curr) {
                case CRATE -> map.setAt(x, y, Tile.FLOOR);
                case CRATE_ON_TARGET -> map.setAt(x, y, Tile.TARGET);
            }

            if (curr.isCrate()) {
                push++;

                switch (next) {
                    case FLOOR -> map.setAt(newX, newY, Tile.CRATE);
                    case TARGET -> map.setAt(newX, newY, Tile.CRATE_ON_TARGET);
                }
            }
        }

        public boolean hasNext() {
            return pathIndex < path.size();
        }

        public void moveBackward() {
            if (!hasPrevious()) {
                return;
            }

            pathIndex--;
            Move move = path.get(pathIndex);

            Direction dir = move.direction();

            if (move.moveCrate()) {
                int crateX = playerX + dir.dirX();
                int crateY = playerY + dir.dirY();

                Tile crate = map.getAt(crateX, crateY);

                switch (crate) {
                    case CRATE -> map.setAt(crateX, crateY, Tile.FLOOR);
                    case CRATE_ON_TARGET -> map.setAt(crateX, crateY, Tile.TARGET);
                }

                switch (map.getAt(playerX, playerY)) {
                    case FLOOR -> map.setAt(playerX, playerY, Tile.CRATE);
                    case TARGET -> map.setAt(playerX, playerY, Tile.CRATE_ON_TARGET);
                }

                push--;
            }

            this.move--;
            playerX -= dir.dirX();
            playerY -= dir.dirY();
        }

        public boolean hasPrevious() {
            return pathIndex > 0;
        }

        private List<Move> computeFullPath() {
            List<Move> path = new ArrayList<>();

            for (int i = 0; i < states.size() - 1; i++) {
                State current = states.get(i);

                if (i != 0) {
                    map.addStateCrates(current);
                }

                int playerX = map.getX(current.playerPos());
                int playerY = map.getY(current.playerPos());

                State next = states.get(i + 1);
                Direction dir = getDirection(current, next);

                int destX = next.playerPos() % map.getWidth() - dir.dirX();
                int destY = next.playerPos() / map.getWidth() - dir.dirY();

                if (playerX != destX || playerY != destY) {
                    path.addAll(findPath(playerX, playerY, destX, destY));
                }

                path.add(new Move(dir, true));

                map.removeStateCrates(current);
            }

            // reset
            map.addStateCrates(states.get(0));

            return path;
        }

        /**
         * Doesn't move any crates
         */
        private List<Move> findPath(int fromX, int fromY, int destX, int destY) {
            Set<Node> visited = new HashSet<>();
            Queue<Node> queue = new ArrayDeque<>();
            queue.offer(new Node(null, fromX, fromY, null));
            visited.add(queue.peek());

            Node solution = null;
            while (!queue.isEmpty() && solution == null) {
                Node node = queue.poll();

                for (Direction direction : Direction.values()) {
                    int newX = node.playerX + direction.dirX();
                    int newY = node.playerY + direction.dirY();

                    if (map.getAt(newX, newY).isSolid()) {
                        continue;
                    }

                    Node child = new Node(node, newX, newY, direction);
                    if (newX == destX && newY == destY) {
                        solution = child;
                        break;
                    }

                    if (visited.add(child)) {
                        queue.offer(child);
                    }
                }
            }

            if (solution == null) {
                throw new IllegalStateException("Can't find path between two states");
            } else {
                List<Move> directions = new ArrayList<>();

                Node n = solution;
                while (n.parent != null) {
                    directions.add(new Move(n.dir, false));

                    n = n.parent;
                }

                Collections.reverse(directions);

                return directions;
            }
        }

        private Direction getDirection(State from, State to) {
            List<Integer> state1Crates = Arrays.stream(from.cratesIndices()).boxed().collect(Collectors.toList());
            List<Integer> state2Crates = Arrays.stream(to.cratesIndices()).boxed().collect(Collectors.toList());

            List<Integer> state1Copy = state1Crates.stream().toList();
            state1Crates.removeAll(state2Crates);
            state2Crates.removeAll(state1Copy);

            // crate position
            int mvt1X = (state1Crates.get(0) % map.getWidth());
            int mvt1Y = (state1Crates.get(0) / map.getWidth());

            // where it goes
            int mvt2X = (state2Crates.get(0) % map.getWidth());
            int mvt2Y = (state2Crates.get(0) / map.getWidth());

            int dirX = mvt2X - mvt1X;
            int dirY = mvt2Y - mvt1Y;

            return Direction.of(dirX, dirY);
        }

        public MutableMap getMap() {
            return map;
        }

        public int getPlayerX() {
            return playerX;
        }

        public int getPlayerY() {
            return playerY;
        }

        public int getMoveCount() {
            return move;
        }

        public int getPushCount() {
            return push;
        }

        public int getTotalMove() {
            return path.size();
        }

        public int getTotalPush() {
            return states.size() - 1;
        }
    }

    private record Node(Node parent, int playerX, int playerY, Direction dir) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            if (playerX != node.playerX) return false;
            return playerY == node.playerY;
        }

        @Override
        public int hashCode() {
            int result = playerX;
            result = 31 * result + playerY;
            return result;
        }
    }

    private record Move(Direction direction, boolean moveCrate) {}
}
