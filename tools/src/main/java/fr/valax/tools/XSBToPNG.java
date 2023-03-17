package fr.valax.tools;

import fr.poulpogaz.json.JsonException;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.args.utils.CommandLineException;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.graphics.style.BoardStyleReader;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.Tile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class XSBToPNG implements Command {

    public static void main(String[] args) {
        try {
            new CommandLineBuilder(new XSBToPNG())
                    .addDefaultConverters()
                    .build()
                    .execute(args);
        } catch (CommandLineException | IOException e) {
            e.printStackTrace();
        }
    }

    @Option(names = {"s", "source"}, defaultValue = "documents/assets", hasArgument = true)
    private Path source;

    @Option(names = {"d", "destination"}, defaultValue = "documents/assets\succnapprox", hasArgument = true)
    private Path dest;

    @Option(names = {"S", "style"}, defaultValue = "styles/warehouse/warehouse.style", hasArgument = true)
    private Path style;

    @Option(names = {"w", "no-overwrite"})
    private boolean noOverwrite;

    @VaArgs
    private String[] only;

    @Override
    public int execute(InputStream in, PrintStream out, PrintStream err) {
        BoardStyle style;
        try {
            style = new BoardStyleReader().read(this.style);
        } catch (IOException e) {
            e.printStackTrace();
            return FAILURE;
        }

        List<Path> paths;
        if (only.length == 0) {
            try (Stream<Path> stream = Files.walk(source)) {
                paths = stream.filter((Path p) -> p.toString().endsWith(".xsb"))
                        .map(p -> source.relativize(p))
                        .toList();
            } catch (IOException e) {
                e.printStackTrace();
                return FAILURE;
            }
        } else {
            paths = new ArrayList<>(only.length);

            for (String path : only) {
                if (path.endsWith(".xsb")) {
                    paths.add(Path.of(path));
                }
            }
        }

        try {
            for (Path p : paths) {
                exportToPNG(style, p);
            }
        } catch (JsonException | IOException e) {
            e.printStackTrace();
            return FAILURE;
        }

        return SUCCESS;
    }

    private void exportToPNG(BoardStyle style, Path levelRelative) throws JsonException, IOException {
        Path level = this.source.resolve(levelRelative);
        Path dest = getDestination(levelRelative);

        if (noOverwrite && Files.exists(dest)) {
            System.out.println(dest + " already exists");
            return;
        }

        if (dest.getParent() != null && !Files.exists(dest.getParent())) {
            Files.createDirectories(dest.getParent());
        }
        System.out.printf("Exporting %s to %s%n", level, dest);

        BufferedImage image = createImage(style, level);
        ImageIO.write(image, "png", dest.toFile());
    }

    private Path getDestination(Path levelRelative) {
        String fileName = levelRelative.getFileName().toString();
        fileName = fileName.substring(0, fileName.length() - 4) + ".png";

        Path parent = levelRelative.getParent();

        if (parent == null) {
            return dest.resolve(fileName);
        } else {
            return dest.resolve(parent).resolve(fileName);
        }
    }

    private BufferedImage createImage(BoardStyle style, Path level) throws IOException {
        SokToPNGTile[][] tiles = readLevel(level);

        int width = tiles[0].length;
        int height = tiles.length;
        int size = style.findBestSize(Integer.MAX_VALUE);

        BufferedImage image = new BufferedImage(size * width, size * height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        try {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    SokToPNGTile tile = tiles[y][x];

                    style.draw(g2d, tile.tile(), tile.player(), tile.direction(), x * size, y * size, size, 1, 1);
                }
            }
        } finally {
            g2d.dispose();
        }

        return image;
    }

    // READING

    private static final Pattern LINE_PATTERN = Pattern.compile("^[#@+$*. -_udlrUDLR]*$");

    private SokToPNGTile[][] readLevel(Path level) throws IOException {
        int width = 0;
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(level)) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }

                if (LINE_PATTERN.matcher(line).matches()) {
                    lines.add(line);
                    width = Math.max(width, line.length());
                }
            }
        }

        SokToPNGTile[][] tiles = new SokToPNGTile[lines.size()][width];

        for (int y = 0; y < lines.size(); y++) {
            String line = lines.get(y);
            int x = 0;
            for (x = 0; x < line.length(); x++) {
                tiles[y][x] = fromChar(line.charAt(x));
            }

            for (; x < width; x++) {
                tiles[y][x] = new SokToPNGTile(Tile.FLOOR);
            }
        }

        return tiles;
    }

    private SokToPNGTile fromChar(char c) {
        return switch (c) {
            case ' ', '-' -> new SokToPNGTile(Tile.FLOOR);
            case '#', '_' -> new SokToPNGTile(Tile.WALL);
            case '$' -> new SokToPNGTile(Tile.CRATE);
            case '.' -> new SokToPNGTile(Tile.TARGET);
            case '*' -> new SokToPNGTile(Tile.CRATE_ON_TARGET);

            case '@' -> new SokToPNGTile(Tile.FLOOR, true, null);
            case '+' -> new SokToPNGTile(Tile.TARGET, true, null);

            case 'u' -> new SokToPNGTile(Tile.FLOOR, true, Direction.UP);
            case 'd' -> new SokToPNGTile(Tile.FLOOR, true, Direction.DOWN);
            case 'l' -> new SokToPNGTile(Tile.FLOOR, true, Direction.LEFT);
            case 'r' -> new SokToPNGTile(Tile.FLOOR, true, Direction.RIGHT);

            case 'U' -> new SokToPNGTile(Tile.TARGET, true, Direction.UP);
            case 'D' -> new SokToPNGTile(Tile.TARGET, true, Direction.DOWN);
            case 'L' -> new SokToPNGTile(Tile.TARGET, true, Direction.LEFT);
            case 'R' -> new SokToPNGTile(Tile.TARGET, true, Direction.RIGHT);
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getShortDescription() {
        return null;
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public boolean addHelp() {
        return true;
    }

    private record SokToPNGTile(Tile tile, boolean player, Direction direction) {

        private SokToPNGTile(Tile tile) {
            this(tile, false, null);
        }
    }
}
