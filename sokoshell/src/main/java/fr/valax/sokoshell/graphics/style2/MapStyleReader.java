package fr.valax.sokoshell.graphics.style2;

import fr.valax.sokoshell.graphics.style.Color;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Tile;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.jline.utils.AttributedStyle.*;

public class MapStyleReader {

    protected final Map<String, Function> functions = new HashMap<>();
    protected final Map<String, Color> reservedColors = new HashMap<>();
    protected final Map<String, StyleFunction> styleFunctions = new HashMap<>();

    protected BufferedReader br;
    protected Path folder;

    protected final Map<String, BufferedImage> images = new HashMap<>();
    protected final Map<String, Color> colorAliases = new HashMap<>();

    protected final Map<String, List<FileMapStyle.Sampler>> samplers = new HashMap<>();
    protected String name;
    protected String author;
    protected String version;

    public MapStyleReader() {
        initFunctions();
        initReservedColors();
        initStyleFunctions();

        for (Tile tile : Tile.values()) {
            samplers.put(tile.name(), new ArrayList<>());
        }
        for (Direction dir : Direction.VALUES) {
            samplers.put(dir.name(), new ArrayList<>());
        }
    }

    private void initFunctions() {
        addFunction(new SimpleSetter("name") {
            @Override
            public void set(String name) {
                MapStyleReader.this.name = name;
            }
        });
        addFunction(new SimpleSetter("author") {
            @Override
            public void set(String author) {
                MapStyleReader.this.author = author;
            }
        });
        addFunction(new SimpleSetter("version") {
            @Override
            public void set(String version) {
                MapStyleReader.this.version = version;
            }
        });
        addFunction(new Alias());
        addFunction(new SetImage());
        addFunction(new SetAnsi());
    }

    private void addFunction(Function func, String... names) {
        for (String name : names) {
            if (functions.put(name, func) != null) {
                throw new IllegalArgumentException("two function with same name");
            }
        }
    }



    private void initReservedColors() {
        reservedColors.put("black", new Color(BLACK));
        reservedColors.put("red", new Color(RED));
        reservedColors.put("green", new Color(GREEN));
        reservedColors.put("yellow", new Color(YELLOW));
        reservedColors.put("blue", new Color(BLUE));
        reservedColors.put("magenta", new Color(MAGENTA));
        reservedColors.put("cyan", new Color(CYAN));
        reservedColors.put("white", new Color(WHITE));

        reservedColors.put("bright_black", new Color(BLACK + BRIGHT));
        reservedColors.put("bright_red", new Color(RED + BRIGHT));
        reservedColors.put("bright_green", new Color(GREEN + BRIGHT));
        reservedColors.put("bright_yellow", new Color(YELLOW + BRIGHT));
        reservedColors.put("bright_blue", new Color(BLUE + BRIGHT));
        reservedColors.put("bright_magenta", new Color(MAGENTA + BRIGHT));
        reservedColors.put("bright_cyan", new Color(CYAN + BRIGHT));
        reservedColors.put("bright_white", new Color(WHITE + BRIGHT));
    }


    private void initStyleFunctions() {
        addStyleFunction(new NoArgumentStyleFunction((style) -> DEFAULT),                   "d", "default");

        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::bold),              "bo", "bold");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::boldOff),           "bo-o", "bold-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::boldDefault),       "bo-d", "bold-default");

        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::faint),             "f", "faint");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::faintDefault),      "f-o", "faint-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::faintOff),          "f-d", "faint-default");

        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::italic),            "it", "italic");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::italicDefault),     "it-o", "italic-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::italicOff),         "it-d", "italic-default");

        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::underline),         "u", "underline");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::underlineDefault),  "u-o", "underline-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::underlineOff),      "u-d", "underline-default");

        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::blink),             "bl", "blink");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::blinkDefault),      "bl-o", "blink-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::blinkOff),          "bl-d", "blink-default");

        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::inverse),           "in", "inverse");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::inverseNeg),        "in-n", "inverse-neg");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::inverseDefault),    "in-o", "inverse-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::inverseOff),        "in-d", "inverse-default");

        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::conceal),           "co", "conceal");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::concealDefault),    "co-o", "conceal-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::concealOff),        "co-d", "conceal-default");

        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::crossedOut),        "cr", "crossed-out");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::crossedOutOff),     "cr-o", "crossed-out-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::crossedOutDefault), "cr-d", "crossed-out-default");

        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::hidden),            "h", "hidden");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::hiddenOff),         "h-o", "hidden-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::hiddenDefault),     "h-d", "hidden-default");

        addStyleFunction(new ColorStyleFunction((style, color) -> color.setFG(style)),    "fg", "foreground");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::foregroundOff),     "fg-o", "foreground-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::foregroundDefault), "fg-d", "foreground-default");

        addStyleFunction(new ColorStyleFunction((style, color) -> color.setBG(style)),    "bg", "background");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::backgroundOff),     "bg-o", "background-off");
        addStyleFunction(new NoArgumentStyleFunction(AttributedStyle::backgroundDefault), "bg-d", "background-default");
    }

    private void addStyleFunction(StyleFunction func, String... names) {
        for (String name : names) {
            if (styleFunctions.put(name, func) != null) {
                throw new IllegalArgumentException("two function with same name");
            }
        }
    }

    public FileMapStyle read(Path file) throws IOException {
        folder = file.getParent();

        try {
            br = Files.newBufferedReader(file);

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#") && !line.isBlank()) {
                    executeFunction(line);
                }
            }

            return new FileMapStyle(this);
        } finally {
            if (br != null) {
                br.close();
            }

            clear();
        }
    }

    private void executeFunction(String line) throws IOException {
        String[] args = line.split(" ");
        Function function = functions.get(args[0]);

        if (function == null) {
            throw new IOException("No function named " + args[0]);
        }

        function.execute(args);
    }

    private void addSampler(String name, FileMapStyle.Sampler sampler) throws IOException {
        switch (name) {
            case "player" -> {
                addSamplerInternalName(Direction.UP.name(), sampler);
                addSamplerInternalName(Direction.DOWN.name(), sampler);
                addSamplerInternalName(Direction.LEFT.name(), sampler);
                addSamplerInternalName(Direction.RIGHT.name(), sampler);
            }
            case "player_up" ->  addSamplerInternalName(Direction.UP.name(), sampler);
            case "player_down" -> addSamplerInternalName(Direction.DOWN.name(), sampler);
            case "player_left" -> addSamplerInternalName(Direction.LEFT.name(), sampler);
            case "player_right" -> addSamplerInternalName(Direction.RIGHT.name(), sampler);
            default -> addSamplerInternalName(name, sampler);
        }
    }

    /**
     * @param name name used by {@link FileMapStyle} ie Tile.name() or Direction.name()
     */
    private void addSamplerInternalName(String name, FileMapStyle.Sampler sampler) throws IOException {
        List<FileMapStyle.Sampler> samplers = this.samplers.get(name);

        if (samplers == null) {
            throw new IOException("No such sampler: " + name);
        }

        samplers.add(sampler);
    }

    private FileMapStyle.Sampler createAnsiSampler(int size) throws IOException {
        AttributedString[] strings = new AttributedString[size];
        AttributedStringBuilder asb = new AttributedStringBuilder();

        for (int i = 0; i < size; i++) {
            String line = br.readLine();

            if (line == null) {
                throw new IOException("Ansi sampler not terminated. (need " + (size - i) + " more line(s)");
            }

            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);

                if (c == '\\') {
                    if (j + 1 < line.length()) {
                        asb.append(line.charAt(j + 1));
                        j++;
                    } else {
                        throw new IOException("Escape character doesn't escape any character");
                    }
                } else if (c == '{') {
                    j = executeStyleFunction(line, asb, j + 1);
                } else {
                    asb.append(c);
                }
            }
        }

        return new FileMapStyle.AnsiSampler(size, strings);
    }

    private int executeStyleFunction(String line, AttributedStringBuilder asb, int i) throws IOException {
        int end = line.indexOf('}', i);

        if (end < 0) {
            throw new IOException("Style function without end");
        }

        String[] split = line.substring(i, end).split(" ");

        for (int j = 0; j < split.length; j++) {
            StyleFunction func = styleFunctions.get(split[j]);

            if (func == null) {
                throw new IOException("No such style function: " + split[j]);
            }

            j = func.execute(split, j + 1, asb);
        }

        return end;
    }

    private Color getColor(String name) {
        Color color = reservedColors.get(name);

        if (color == null) {
            return colorAliases.get(name);
        } else {
            return color;
        }
    }

    private int parseInt(String str) throws IOException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new IOException("Failed to parse %s to int".formatted(str), e);
        }
    }

    private BufferedImage getImage(String path) throws IOException {
        BufferedImage image = images.get(path);

        if (image == null) {
            image = ImageIO.read(folder.resolve(path).toFile());
            images.put(path, image);
        }

        return image;
    }

    private void clear() {
        images.clear();
        colorAliases.clear();

        for (List<FileMapStyle.Sampler> s : samplers.values()) {
            s.clear();
        }

        folder = null;
        name = null;
        author = null;
        version = null;
    }


    private abstract static class Function {

        protected final String name;

        public Function(String name) {
            this.name = name;
        }

        /**
         * @param args argument to the function, also include the command
         * @throws IOException if an I/O error occurs
         */
        public abstract void execute(String[] args) throws IOException;

        public String getName() {
            return name;
        }
    }

    private abstract static class SimpleSetter extends Function {

        public SimpleSetter(String name) {
            super(name);
        }

        @Override
        public void execute(String[] args) throws IOException {
            if (args.length != 2) {
                set(args[1]);
            } else {
                throw new IOException("Invalid number of argument for " + name + ": expected 1, got: " + (args.length - 1));
            }
        }

        public abstract void set(String name);
    }

    private class Alias extends Function {

        public Alias() {
            super("alias");
        }

        @Override
        public void execute(String[] args) throws IOException {
            if (args.length != 3 && args.length != 5) {
                throw new IOException("""
                        Invalid use of alias, expected 2 or 4 arguments, got: %d.
                        Prototype: alias NAME COLOR_INDEX or alias NAME RED GREEN BLUE
                        """.formatted(args.length - 1));
            } else {
                String name = args[1];

                if (reservedColors.containsKey(name)) {
                    throw new IOException("Reserved color: " + name);
                }

                try {
                    Integer.parseInt(args[1]);

                    throw new IOException("NAME cannot be a number");
                } catch (NumberFormatException e) {
                    // ignored
                }

                Color color;
                if (args.length == 3) {
                    color = getColor(args[2]);

                    if (color == null) {
                        throw new IOException("No such color (alias): " + args[2]);
                    }
                } else {
                    color = new Color(parseInt(args[2]), parseInt(args[3]), parseInt(args[4]));
                }

                colorAliases.put(name, color);
            }
        }
    }

    private class SetImage extends Function {

        public SetImage() {
            super("set-image");
        }

        @Override
        public void execute(String[] args) throws IOException {
            if (args.length != 6) {
                throw new IOException("""
                        Invalid use of set-image, expected 5 arguments, got: %d.
                        Prototype: set-image TILE/PLAYER_DIR SIZE PATH_TO_IMAGE X Y
                        """.formatted(args.length - 1));
            }

            int size = parseInt(args[2]);
            if (size <= 0) {
                throw new IOException("Zero or negative size: " + size);
            }

            BufferedImage image = getImage(args[3]);
            int x = parseInt(args[4]);
            int y = parseInt(args[5]);

            FileMapStyle.Sampler sampler = new FileMapStyle.ImageSampler(image.getSubimage(x, y, size, size));
            addSampler(args[1], sampler);
        }
    }

    private class SetAnsi extends Function {

        public SetAnsi() {
            super("set-ansi");
        }

        @Override
        public void execute(String[] args) throws IOException {
            if (args.length != 6) {
                throw new IOException("""
                        Invalid use of set-image, expected 2 arguments, got: %d.
                        Prototype: set-image TILE/PLAYER_DIR SIZE
                        The SIZE following lines are used for the sampler
                        """.formatted(args.length - 1));
            }

            int size = parseInt(args[2]);
            if (size <= 0) {
                throw new IOException("Zero or negative size: " + size);
            }

            FileMapStyle.Sampler sampler = createAnsiSampler(size);
            addSampler(args[1], sampler);
        }
    }

    @FunctionalInterface
    private interface StyleFunction {

        int execute(String[] args, int index, AttributedStringBuilder asb) throws IOException;
    }

    private static class NoArgumentStyleFunction implements StyleFunction {

        private final java.util.function.Function<AttributedStyle, AttributedStyle> function;

        public NoArgumentStyleFunction(java.util.function.Function<AttributedStyle, AttributedStyle> function) {
            this.function = function;
        }

        @Override
        public int execute(String[] args, int index, AttributedStringBuilder asb) {
            asb.style(function.apply(asb.style()));
            return index;
        }
    }

    private class ColorStyleFunction implements StyleFunction {

        private final BiFunction<AttributedStyle, Color, AttributedStyle> function;

        public ColorStyleFunction(BiFunction<AttributedStyle, Color, AttributedStyle> function) {
            this.function = function;
        }

        @Override
        public int execute(String[] args, int i, AttributedStringBuilder asb) throws IOException {
            int add;
            Color color;
            try {
                int red = Integer.parseInt(args[i]);

                color = new Color(red, parseInt(args[i + 1]), parseInt(args[i + 2]));
                add = 3;
            } catch (NumberFormatException e) {
                // not a number, either an alias or a reserved color

                color = getColor(args[i]);
                if (color == null) {
                    throw new IOException("No such color: " + args[i]);
                }

                add = 1;
            }

            asb.style(function.apply(asb.style(), color));

            return i + add;
        }
    }
}
