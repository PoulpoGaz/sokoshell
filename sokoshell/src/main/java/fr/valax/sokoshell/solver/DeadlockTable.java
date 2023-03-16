package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.graphics.style.BasicStyle;
import fr.valax.sokoshell.readers.XSBReader;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class DeadlockTable {

    protected static final int NOT_A_DEADLOCK = 0;
    protected static final int MAYBE_A_DEADLOCK = 1;
    protected static final int A_DEADLOCK = 2;

    protected static final DeadlockTable DEADLOCK = new DeadlockTable(A_DEADLOCK);
    protected static final DeadlockTable NOT_DEADLOCK = new DeadlockTable(NOT_A_DEADLOCK);

    protected final int deadlock;

    protected final int x; // relative to player x
    protected final int y; // relative to player y
    protected final DeadlockTable floorChild;
    protected final DeadlockTable wallChild;
    protected final DeadlockTable crateChild;

    private DeadlockTable(int deadlock) {
        this(deadlock, -1, -1, null, null, null);
    }

    public DeadlockTable(int deadlock, int x, int y,
                         DeadlockTable floorChild, DeadlockTable wallChild, DeadlockTable crateChild) {
        this.deadlock = deadlock;
        this.x = x;
        this.y = y;
        this.floorChild = floorChild;
        this.wallChild = wallChild;
        this.crateChild = crateChild;
    }

    public boolean isDeadlock(TileInfo player, Direction pushDir) {
        Board board = player.getBoard();

        if (player.adjacent(pushDir).isCrateOnTarget()) {
            return false;
        }

        return switch (pushDir) {
            case LEFT -> isDeadlock((t) -> board.safeGetAt(player.getX() + t.y, player.getY() + t.x));
            case UP -> isDeadlock((t) -> board.safeGetAt(player.getX() + t.x, player.getY() + t.y));
            case RIGHT -> isDeadlock((t) -> board.safeGetAt(player.getX() - t.y, player.getY() - t.x));
            case DOWN -> isDeadlock((t) -> board.safeGetAt(player.getX() - t.x, player.getY() - t.y));
        };
    }

    private boolean isDeadlock(Function<DeadlockTable, TileInfo> getTile) {
        if (deadlock == A_DEADLOCK) {
            return true;
        } else if (deadlock == NOT_A_DEADLOCK) {
            return false;
        }

        TileInfo tile = getTile.apply(this);

        if (tile == null) {
            return false;
        }

        return switch (tile.getTile()) {
            case FLOOR -> floorChild.isDeadlock(getTile);
            case WALL -> wallChild.isDeadlock(getTile);
            case CRATE -> crateChild.isDeadlock(getTile);
            default -> false;
        };
    }

    public static void write(DeadlockTable root, Path out) throws IOException {
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(out))) {
            Stack<DeadlockTable> stack = new Stack<>();
            stack.push(root);

            while (!stack.isEmpty()) {
                DeadlockTable table = stack.pop();

                os.write(table.deadlock);
                if (table.deadlock == MAYBE_A_DEADLOCK) {
                    writeInt(os, table.x);
                    writeInt(os, table.y);
                    stack.push(table.crateChild);
                    stack.push(table.wallChild);
                    stack.push(table.floorChild);
                }
            }
        }
    }

    public static DeadlockTable read(Path in) throws IOException {
        try (InputStream is = new BufferedInputStream(Files.newInputStream(in))) {
            return read(is);
        }
    }

    private static DeadlockTable read(InputStream is) throws IOException {
        int i = is.read();

        if (i < 0 || i > 2) {
            throw new IOException("Malformed table");
        }

        if (i == A_DEADLOCK) {
            return DEADLOCK;
        } else if (i == NOT_A_DEADLOCK) {
            return NOT_DEADLOCK;
        } else {
            int x = readInt(is);
            int y = readInt(is);

            DeadlockTable floor = read(is);
            DeadlockTable wall = read(is);
            DeadlockTable crate = read(is);

            return new DeadlockTable(MAYBE_A_DEADLOCK, x, y, floor, wall, crate);
        }
    }

    private static void writeInt(OutputStream os, int val) throws IOException {
        os.write(val & 0xFF);
        os.write((val >> 8) & 0xFF);
        os.write((val >> 16) & 0xFF);
        os.write((val >> 24) & 0xFF);
    }

    private static int readInt(InputStream is) throws IOException {
        int a = is.read() & 0xFF;
        int b = is.read() & 0xFF;
        int c = is.read() & 0xFF;
        int d = is.read() & 0xFF;

        return (d << 24) | (c << 16) | (b << 8) | a;
    }

    public static int countNotDetectedDeadlock(DeadlockTable table, int size) {
        Board board = createBoard(size);

        // no dead tiles by default
        board.setAt(1, 1, Tile.TARGET);
        board.setAt(board.getWidth() - 2, board.getHeight() - 2, Tile.TARGET);

        board.computeFloors();
        board.computeDeadTiles();
        board.setAt(board.getWidth() / 2, board.getHeight() - 4, Tile.CRATE);

        return countNotDetectedDeadlock(table, board, board.getWidth() / 2, board.getHeight() - 3);
    }

    private static int countNotDetectedDeadlock(DeadlockTable table, Board board, int playerX, int playerY) {
        if (table.deadlock == A_DEADLOCK) {
            State state = createState(board, playerX, playerY);

            if (FreezeDeadlockDetector.checkFreezeDeadlock(board, state)) {
                return 0;
            }

            CorralDetector detector = board.getCorralDetector();
            detector.findCorral(board, playerX, playerY);
            detector.findPICorral(board, state.cratesIndices());

            for (Corral c : detector.getCorrals()) {
                if (c.isDeadlock(state)) {
                    return 0;
                }
            }

            BasicStyle.XSB_STYLE.print(board, playerX, playerY);

            return 1; // not detected !
        } else if (table.deadlock == MAYBE_A_DEADLOCK) {
            int n = countNotDetectedDeadlock(table.floorChild, board, playerX, playerY);

            board.setAt(playerX + table.x, playerY + table.y, Tile.WALL);
            n += countNotDetectedDeadlock(table.wallChild, board, playerX, playerY);

            board.setAt(playerX + table.x, playerY + table.y, Tile.CRATE);
            n += countNotDetectedDeadlock(table.crateChild, board, playerX, playerY);

            board.setAt(playerX + table.x, playerY + table.y, Tile.FLOOR);

            return n;
        } else {
            return 0;
        }
    }


    public static DeadlockTable generate(int size) {
        // if size = 3, returned board looks like:
        // #######
        // #     #
        // #     #
        // #     #
        // #  @  #
        // #######
        // size of generated pattern: size * size
        Board board = createBoard(size);

        board.setAt(board.getWidth() / 2, board.getHeight() - 4, Tile.CRATE);

        return generate(board, createOrder(size), 0, board.getWidth() / 2, board.getHeight() - 3);
    }

    public static DeadlockTable generate2(int size, int nThread) {
        Board board = createBoard(size);

        board.setAt(board.getWidth() / 2, board.getHeight() - 4, Tile.CRATE);

        ForkJoinPool pool = new ForkJoinPool(nThread <= 0 ? Runtime.getRuntime().availableProcessors() :  nThread);
        GenerateDeadlockTableTask task = new GenerateDeadlockTableTask(board, createOrder(size), 0, board.getWidth() / 2, board.getHeight() - 3, false);

        DeadlockTable table = pool.invoke(task);
        pool.shutdown();

        return table;
    }


    private static DeadlockTable generate(Board board, int[][] order, int index, int playerX, int playerY) {
        // BasicStyle.XSB_STYLE.print(board, playerX, playerY);

        if (isDeadlock_(board, playerX, playerY)) {
            return DEADLOCK;
        } else if (index < order.length) {
            int relativeX = order[index][0];
            int relativeY = order[index][1];

            board.setAt(playerX + relativeX, playerY + relativeY, Tile.WALL);
            DeadlockTable wallChild = generate(board, order, index + 1, playerX, playerY);

            board.setAt(playerX + relativeX, playerY + relativeY, Tile.CRATE);
            DeadlockTable crateChild = generate(board, order, index + 1, playerX, playerY);

            board.setAt(playerX + relativeX, playerY + relativeY, Tile.FLOOR);
            if (wallChild == NOT_DEADLOCK && crateChild == NOT_DEADLOCK) {
                return NOT_DEADLOCK;
            }

            DeadlockTable floorChild = generate(board, order, index + 1, playerX, playerY);

            return new DeadlockTable(MAYBE_A_DEADLOCK, relativeX, relativeY, floorChild, wallChild, crateChild);
        } else {
            return NOT_DEADLOCK;
        }
    }

    private static Board createBoard(int size) {
        Board board = new MutableBoard(size + 4, size + 4);
        State.initZobristValues(board.getWidth() * board.getHeight());

        for (int x = 0; x < board.getWidth(); x++) {
            board.setAt(x, 0, Tile.WALL);
            board.setAt(x, board.getHeight() - 1, Tile.WALL);
        }

        for (int y = 0; y < board.getHeight(); y++) {
            board.setAt(0, y, Tile.WALL);
            board.setAt(board.getWidth() - 1, y, Tile.WALL);
        }

        return board;
    }

    protected static int[][] createOrder(int size) {
        int[][] order = new int[size * size - 2][2];

        boolean odd = size % 2 == 1;
        int i = 0;
        int half = size / 2;
        for (int y = 0; y > -size; y--) {
            for (int x = -half; x < half || (x == half && odd); x++) {
                if (x == 0 && (y == 0 || y == -1)) {
                    continue;
                }

                order[i] = new int[] {x, y};
                i++;
            }
        }


        return order;
    }

    private static class GenerateDeadlockTableTask extends RecursiveTask<DeadlockTable> {

        private static final AtomicInteger COUNTER = new AtomicInteger();
        private static final int total = 4_782_969;

        private final Board board;
        private final int[][] order;
        private final int index;
        private final int playerX;
        private final int playerY;
        private final boolean check;

        public GenerateDeadlockTableTask(Board board, int[][] order, int index, int playerX, int playerY, boolean check) {
            this.board = board;
            this.order = order;
            this.index = index;
            this.playerX = playerX;
            this.playerY = playerY;
            this.check = check;
        }

        @Override
        protected DeadlockTable compute() {
            int n = COUNTER.incrementAndGet();

            if (n % 10_000 == 0) {
                System.out.printf("%.2f%% - %d%n", 100f * n / total, n);
            }

            if (check && isDeadlock_(board, playerX, playerY)) {
                return DEADLOCK;
            } else if (index < order.length) {
                int relativeX = order[index][0];
                int relativeY = order[index][1];

                GenerateDeadlockTableTask wall = subTask(index, Tile.WALL, true);
                GenerateDeadlockTableTask crate = subTask(index, Tile.CRATE, true);

                wall.fork();
                crate.fork();

                DeadlockTable wallChild = wall.join();
                DeadlockTable crateChild = crate.join();

                if (wallChild == NOT_DEADLOCK && crateChild == NOT_DEADLOCK) {
                    return NOT_DEADLOCK;
                }

                GenerateDeadlockTableTask floor = subTask(index, Tile.FLOOR, false);
                DeadlockTable floorChild = floor.fork().join();

                // the three are never equals to deadlock because
                // it means the current board is a deadlock, and
                // it must be detected by isDeadlock_
                return new DeadlockTable(MAYBE_A_DEADLOCK, relativeX, relativeY, floorChild, wallChild, crateChild);

            } else {
                return NOT_DEADLOCK;
            }
        }

        private GenerateDeadlockTableTask subTask(int index, Tile replacement, boolean check) {
            MutableBoard board = new MutableBoard(this.board);
            int relativeX = order[index][0];
            int relativeY = order[index][1];

            board.setAt(playerX + relativeX, playerY + relativeY, replacement);

            return new GenerateDeadlockTableTask(board, order, index + 1, playerX, playerY, check);
        }
    }





    private static boolean isDeadlock_(Board board, int playerX, int playerY) {
        State first = createState(board, playerX, playerY);

        ReachableTiles reachableTiles = new ReachableTiles(board);
        HashSet<State> visited = new HashSet<>();
        Queue<State> toVisit = new ArrayDeque<>();

        visited.add(first);
        toVisit.offer(first);

        boolean deadlock = true;
        while (!toVisit.isEmpty() && deadlock) {
            State parent = toVisit.poll();

            board.addStateCrates(parent);

            if (FreezeDeadlockDetector.checkFreezeDeadlock(board, parent)) {
                board.removeStateCrates(parent);
                continue;
            }

            reachableTiles.findReachableCases(board.getAt(parent.playerPos()));
            deadlock = addChildrenStates(reachableTiles, parent, board, visited, toVisit);
            board.removeStateCrates(parent);
        }

        board.addStateCrates(first);

        return deadlock;
    }

    private static boolean addChildrenStates(ReachableTiles reachableTiles, State parent,
                                             Board board, Set<State> visited, Queue<State> toVisit) {
        for (int i = 0; i < parent.cratesIndices().length; i++) {
            TileInfo crate = board.getAt(parent.cratesIndices()[i]);

            for (Direction dir : Direction.VALUES) {
                TileInfo player = crate.adjacent(dir.negate());

                if (!reachableTiles.isReachable(player)) {
                    continue;
                }

                TileInfo dest = crate.adjacent(dir);
                if (dest.isSolid()) {
                    continue;
                }

                State child;
                if (dest.getY() == 1 || dest.getX() == 1 || dest.getX() == board.getWidth() - 2) {
                    // remove the crate, it is outside the pattern
                    if (parent.cratesIndices().length == 1) {
                        return false; // all crates were moved outside the pattern. not a deadlock...
                    }

                    int topLeft = board.topLeftReachablePosition(crate, board.getAt(0, 0));

                    child = new State(topLeft, copyRemoveOneElement(parent.cratesIndices(), i), parent);

                } else {
                    int topLeft = board.topLeftReachablePosition(crate, dest);
                    child = parent.child(topLeft, i, dest.getIndex());
                }

                if (visited.add(child)) {
                    toVisit.add(child);
                }
            }
        }

        return true; // not a deadlock
    }

    private static int[] copyRemoveOneElement(int[] array, int indexToRemove) {
        int[] newArray = new int[array.length - 1];

        int offset = 0;
        for (int i = 0; i < array.length; i++) {
            if (indexToRemove == i) {
                offset = 1;
            } else {
                newArray[i - offset] = array[i];
            }
        }

        return newArray;
    }

    private static State createState(Board board, int playerX, int playerY) {
        List<Integer> ints = new ArrayList<>();

        board.forEach(t -> {
            if (t.anyCrate()) {
                ints.add(t.getIndex());
            }
        });

        return new State(playerY * board.getWidth() + playerX, ints.stream().mapToInt(i -> i).toArray(), null);
    }
}
