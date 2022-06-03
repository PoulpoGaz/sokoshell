package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.solver.Map;
import org.jline.console.impl.Builtins;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.util.*;
import java.util.stream.Collectors;

public class SolutionCommand extends AbstractVoidCommand {

    @Option(names = {"p", "-pack"}, hasArgument = true, argName = "Pack name", optional = false)
    private String name;

    @Option(names = {"i", "-index"}, hasArgument = true, argName = "Level index", optional = false)
    private int index;

    public SolutionCommand(SokoShellHelper helper) {
        super(helper);
    }

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
            System.out.println("Not solverd");
            return;
        }

        showAnimation(new SolutionAnimator(l));
    }

    private void showAnimation(SolutionAnimator animator) {
        Terminal terminal = helper.getTerminal();

        while (animator.hasNext()) {
            animator.move();

            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
            PrintCommand.printMap(animator.getMap(), animator.getPlayerIndex());

            long time = System.currentTimeMillis();

            while (time + 100 > System.currentTimeMillis()) {
                Thread.onSpinWait();
            }
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

    public static class SolutionAnimator {

        private final MutableMap map;
        private final List<State> states;
        private int stateIndex = -1;

        private List<Direction> path;
        private int pathIndex;

        private int playerX;
        private int playerY;

        public SolutionAnimator(Level level) {
            this.states = level.getSolution().getStates();
            this.map = new MutableMap(level.getMap());
            this.playerX = level.getPlayerX();
            this.playerY = level.getPlayerY();
            stateIndex = 0;
        }

        public void move() {
            if (stateIndex < 0) {
                stateIndex = 0;
            } else if (path == null) {
                State current = states.get(stateIndex);
                State next = states.get(stateIndex + 1);
                Direction dir = getDirection(current, next);

                int destX = next.playerPos() % map.getWidth() - dir.dirX();
                int destY = next.playerPos() / map.getWidth() - dir.dirY();

                path = findPath(playerX, playerY, destX, destY);
                path.add(dir);
                pathIndex = 0;
            }

            if (path != null) {
                Direction dir = path.get(pathIndex);

                move(dir);

                pathIndex++;
                if (pathIndex >= path.size()) {
                    path = null;
                }
            }
        }

        private void move(Direction dir) {
            playerX += dir.dirX();
            playerY += dir.dirY();

            moveCrate(playerX, playerY, dir);
        }

        // move crate if needed
        private void moveCrate(int x, int y, Direction dir) {
            int newX = x + dir.dirX();
            int newY = y + dir.dirY();

            Tile curr = map.getAt(x, y);
            Tile next = map.getAt(newX, newY);

            switch (curr) {
                case CRATE -> map.setAt(x, y, Tile.FLOOR);
                case CRATE_ON_TARGET -> map.setAt(x, y, Tile.CRATE_ON_TARGET);
            }

            if (curr.isCrate()) {
                switch (next) {
                    case FLOOR -> map.setAt(newX, newY, Tile.CRATE);
                    case TARGET -> map.setAt(newX, newY, Tile.CRATE_ON_TARGET);
                }
            }
        }

        public boolean hasNext() {
            if (path != null) {
                return true;
            } else {
                return stateIndex < states.size();
            }
        }

        /**
         * Doesn't move any crates
         */
        public List<Direction> findPath(int fromX, int fromY, int destX, int destY) {
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

                    if (newX == destX && newY == destY) {
                        solution = node;
                        break;
                    }

                    Node child = new Node(node, newX, newY, direction);
                    if (visited.add(child)) {
                        queue.offer(child);
                    }
                }
            }

            if (solution == null) {
                throw new IllegalStateException("Can't find path betwen two states");
            } else {
                List<Direction> directions = new ArrayList<>();

                Node n = solution;
                while (n.parent != null) {
                    directions.add(n.dir);

                    n = n.parent;
                }

                Collections.reverse(directions);

                return directions;
            }
        }

        public Direction getDirection(State from, State to) {
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

        public int getPlayerIndex() {
            return playerY * map.getWidth() + playerX;
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
}
