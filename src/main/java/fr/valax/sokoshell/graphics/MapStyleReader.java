package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static fr.valax.sokoshell.graphics.MapStyle.Element;
import static org.jline.utils.AttributedStyle.*;

public class MapStyleReader {

    private final Map<String, Color> reservedColors;
    private final Map<String, StyleFunction> styleFunctions;

    private final Map<String, BufferedImage> images = new HashMap<>();
    private final Map<String, Color> colorAliases = new HashMap<>();

    private Path folder;
    private BufferedReader br;
    private Tokenizer tokenizer;

    private String name;
    private String author;
    private String version;
    private Map<Integer, Map<Element, TileStyle>> styles;

    public MapStyleReader() {
        reservedColors = initReservedColors();

        styleFunctions = new HashMap<>();
        initStyleFunctions();
    }

    private Map<String, Color> initReservedColors() {
        Map<String, Color> reservedColors = new HashMap<>();

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

        return reservedColors;
    }


    private void initStyleFunctions() {
        addStyleFunction((style, t) -> DEFAULT,                   "d", "default");

        addStyleFunction((style, t) -> style.bold(),              "bo", "bold");
        addStyleFunction((style, t) -> style.boldOff(),           "bo-o", "bold-off");
        addStyleFunction((style, t) -> style.boldDefault(),       "bo-d", "bold-default");

        addStyleFunction((style, t) -> style.faint(),             "f", "faint");
        addStyleFunction((style, t) -> style.faintDefault(),      "f-o", "faint-off");
        addStyleFunction((style, t) -> style.faintOff(),          "f-d", "faint-default");

        addStyleFunction((style, t) -> style.italic(),            "it", "italic");
        addStyleFunction((style, t) -> style.italicDefault(),     "it-o", "italic-off");
        addStyleFunction((style, t) -> style.italicOff(),         "it-d", "italic-default");

        addStyleFunction((style, t) -> style.underline(),         "u", "underline");
        addStyleFunction((style, t) -> style.underlineDefault(),  "u-o", "underline-off");
        addStyleFunction((style, t) -> style.underlineOff(),      "u-d", "underline-default");

        addStyleFunction((style, t) -> style.blink(),             "bl", "blink");
        addStyleFunction((style, t) -> style.blinkDefault(),      "bl-o", "blink-off");
        addStyleFunction((style, t) -> style.blinkOff(),          "bl-d", "blink-default");

        addStyleFunction((style, t) -> style.inverse(),           "in", "inverse");
        addStyleFunction((style, t) -> style.inverseNeg(),        "in-n", "inverse-neg");
        addStyleFunction((style, t) -> style.inverseDefault(),    "in-o", "inverse-off");
        addStyleFunction((style, t) -> style.inverseOff(),        "in-d", "inverse-default");

        addStyleFunction((style, t) -> style.conceal(),           "co", "conceal");
        addStyleFunction((style, t) -> style.concealDefault(),    "co-o", "conceal-off");
        addStyleFunction((style, t) -> style.concealOff(),        "co-d", "conceal-default");

        addStyleFunction((style, t) -> style.crossedOut(),        "cr", "crossed-out");
        addStyleFunction((style, t) -> style.crossedOutOff(),     "cr-o", "crossed-out-off");
        addStyleFunction((style, t) -> style.crossedOutDefault(), "cr-d", "crossed-out-default");

        addStyleFunction((style, t) -> style.hidden(),            "h", "hidden");
        addStyleFunction((style, t) -> style.hiddenOff(),         "h-o", "hidden-off");
        addStyleFunction((style, t) -> style.hiddenDefault(),     "h-d", "hidden-default");

        addStyleFunction((style, t) -> getColor(t).setFG(style),   "fg", "foreground");
        addStyleFunction((style, t) -> style.foregroundOff(),     "fg-o", "foreground-off");
        addStyleFunction((style, t) -> style.foregroundDefault(), "fg-d", "foreground-default");

        addStyleFunction((style, t) -> getColor(t).setBG(style),   "bg", "background");
        addStyleFunction((style, t) -> style.backgroundOff(),     "bg-o", "background-off");
        addStyleFunction((style, t) -> style.backgroundDefault(), "bg-d", "background-default");
    }

    private void addStyleFunction(StyleFunction func, String... names) {
        for (String name : names) {
            if (styleFunctions.put(name, func) != null) {
                throw new IllegalArgumentException("two function with same name");
            }
        }
    }

    public MapStyle read(Path file) throws IOException {
        images.clear();
        colorAliases.clear();
        folder = file.getParent();

        name = null;
        author = null;
        version = null;
        styles = new HashMap<>();

        try {
            br = Files.newBufferedReader(file);

            tokenizer = new Tokenizer(br);
            while (tokenizer.hasNext() || tokenizer.nextLine()) {
                parseCurrentLine();
            }

            return new MapStyle(name, author, version, styles);
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    private void parseCurrentLine() throws IOException {
         Token next = tokenizer.next();

        if (next.getType() == TokenType.SECTION_START) {
            String sectionName = nextToken(TokenType.WORD);
            nextToken(TokenType.SECTION_END);

            switch (sectionName) {
                case "alias" -> parseAlias();
                case "size" -> parseSize();
                default -> throw error("Unknown section type: %s", sectionName);
            }

        } else if (next.getType() == TokenType.WORD) {
            switch (next.getValue()) {
                case "name" -> name = lastWord();
                case "author" -> author = lastWord();
                case "version" -> version = lastWord();
                default -> throw error("Unknown key: %s", next.getValue());
            }
        }
    }

    private String nextToken(TokenType expectedType) throws IOException {
        if (tokenizer.hasNext()) {
            Token next = tokenizer.next();

            if (next.getType() != expectedType) {
                throw error("Expecting %s but was%s", TokenType.WORD, next.getType());
            }

            return next.getValue();
        }

        throw error("Expecting %s but was EOL", TokenType.WORD);
    }

    private String lastWord() throws IOException {
        if (tokenizer.hasNext()) {
            Token next = tokenizer.next();

            if (next.getType() != TokenType.WORD) {
                throw error("Expecting %s but was%s", TokenType.WORD, next.getType());
            }

            String word = next.getValue();

            if (tokenizer.hasNext()) {
                throw error("not EOL");
            }

            return word;
        }

        throw error("Expecting %s but was EOL", TokenType.WORD);
    }

    // ALIAS

    private void parseAlias() throws IOException {
        if (tokenizer.hasNext()) {
            throw error("not EOL");
        }

        while (tokenizer.nextLine()) {
            if (tokenizer.hasNext()) {
                Token token = tokenizer.next();

                if (token.getType() == TokenType.WORD) {

                    String name = token.getValue();

                    if (reservedColors.containsKey(name)) {
                        throw error("reserved color: %s", name);
                    }

                    colorAliases.put(name, getColor(tokenizer));

                } else {
                    tokenizer.reset();
                    break;
                }
            }
        }
    }

    // SIZE

    private void parseSize() throws IOException {
        Map<Element, TileStyle> tileStyles = new HashMap<>();

        int size = parseInt(lastWord());

        while (tokenizer.nextLine()) {
            nextToken(TokenType.SECTION_START);
            String tile = nextToken(TokenType.WORD);
            Element[] elements = getElements(tile);

            if (elements == null) {
                tokenizer.reset();
                break;
            }

            nextToken(TokenType.SECTION_END);

            String styleType = nextToken(TokenType.WORD);
            TileStyle mergeWith = null;
            boolean floorTargetMerge = false;

            if (tokenizer.hasNext()) {
                if (!nextToken(TokenType.WORD).equals("merge")) {
                    throw error("Expecting merge");
                }

                String last = lastWord();

                if (last.equals("floor-target")) {
                    floorTargetMerge = true;
                } else {
                    mergeWith = tileStyles.get(getElement(last));

                    if (mergeWith == null) {
                        throw error("No style named %s is loaded", last);
                    }
                }
            }

            TileStyle tileStyle;
            if (styleType.equals("ansi")) {
                tileStyle = parseAnsi(size);
            } else if (styleType.equals("image")) {
                tileStyle = parseImage(size);
            } else {
                throw error("Unknown style type: %s", styleType);
            }

            // do merge
            if (floorTargetMerge) {
                TileStyle floor = tileStyles.get(Element.FLOOR);
                TileStyle target = tileStyles.get(Element.TARGET);

                TileStyle floorMerged = floor.merge(tileStyle);
                TileStyle targetMerged = target.merge(tileStyle);

                for (Element e : elements) {
                    if (e.name().contains("TARGET")) {
                        tileStyles.put(e, targetMerged);
                    } else {
                        tileStyles.put(e, floorMerged);
                    }
                }
            } else {
                if (mergeWith != null) {
                    tileStyle = mergeWith.merge(tileStyle);
                }

                for (Element e : elements) {
                    tileStyles.put(e, tileStyle);
                }
            }
        }

        if (tileStyles.size() != Element.values().length) {
            throw error("Incomplete style for size: %d", size);
        }

        styles.put(size, tileStyles);
    }

    private Element[] getElements(String tile) {
        return switch (tile) {
            case "player" -> new Element[] {
                    Element.PLAYER_FLOOR_RIGHT,
                    Element.PLAYER_FLOOR_DOWN,
                    Element.PLAYER_FLOOR_LEFT,
                    Element.PLAYER_FLOOR_UP,
                    Element.PLAYER_ON_TARGET_RIGHT,
                    Element.PLAYER_ON_TARGET_DOWN,
                    Element.PLAYER_ON_TARGET_LEFT,
                    Element.PLAYER_ON_TARGET_UP};
            case "player_down" -> new Element[] {Element.PLAYER_FLOOR_DOWN, Element.PLAYER_ON_TARGET_DOWN};
            case "player_up" -> new Element[] {Element.PLAYER_FLOOR_UP, Element.PLAYER_ON_TARGET_UP};
            case "player_left" -> new Element[] {Element.PLAYER_FLOOR_LEFT, Element.PLAYER_ON_TARGET_LEFT};
            case "player_right" -> new Element[] {Element.PLAYER_FLOOR_RIGHT, Element.PLAYER_ON_TARGET_RIGHT};
            case "player_on_target" -> new Element[] {
                    Element.PLAYER_ON_TARGET_RIGHT,
                    Element.PLAYER_ON_TARGET_DOWN,
                    Element.PLAYER_ON_TARGET_LEFT,
                    Element.PLAYER_ON_TARGET_UP};
            case "player_floor" -> new Element[] {
                    Element.PLAYER_FLOOR_RIGHT,
                    Element.PLAYER_FLOOR_DOWN,
                    Element.PLAYER_FLOOR_LEFT,
                    Element.PLAYER_FLOOR_UP};
            default -> {
                Element e = getElement(tile);

                if (e == null) {
                    yield null;
                } else {
                    yield new Element[] {e};
                }
            }
        };
    }

    private Element getElement(String tile) {
        try {
            return Element.valueOf(tile.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ANSI

    private TileStyle parseAnsi(int size) throws IOException {
        AnsiTokenizer tokenizer = new AnsiTokenizer(br);

        AttributedString[] strings = new AttributedString[size];
        AttributedStringBuilder asb = new AttributedStringBuilder();

        for (int i = 0; i < size; i++) {
            if (!tokenizer.nextLine() || !tokenizer.hasNext()) {
                throw error(tokenizer, "Incomplete style for size: %d", size);
            }

            asb.setLength(0);

            while (tokenizer.hasNext()) {
                Token next = tokenizer.next();

                if (next.getType() == TokenType.WORD) {
                    if (tokenizer.isInFunction()) {
                        StyleFunction func = styleFunctions.get(next.getValue());

                        if (func == null) {
                            throw error(tokenizer, "Unknown function: %s", next.value);
                        }

                        asb.style(func.apply(asb.style(), tokenizer));
                    } else {
                        asb.append(next.getValue());
                    }
                }
            }

            if (tokenizer.isInFunction()) {
                throw error(tokenizer, "no end");
            }

            strings[i] = asb.toAttributedString();
        }

        return new AnsiTile(size, strings);
    }

    private Color getColor(LineByLineTokenizer tokenizer) throws IOException {
        Token next = tokenizer.peek();

        if (next == null || next.getType() != TokenType.WORD) {
            throw error(tokenizer, "Invalid color");
        }
        tokenizer.next();

        Color color = getColor(next.getValue());
        if (color != null) {
            return color;
        }

        int red = parseInt(next.getValue());

        if (!tokenizer.hasNext()) {
            throw error(tokenizer, "Expecting green after red");
        }
        int green = parseInt(tokenizer.next().getValue());

        if (!tokenizer.hasNext()) {
            throw error(tokenizer, "Expecting blue after green");
        }
        int blue = parseInt(tokenizer.next().getValue());

        return new Color(red, green, blue);
    }

    private Color getColor(String name) {
        Color color = reservedColors.get(name);

        if (color == null) {
            return colorAliases.get(name);
        } else {
            return color;
        }
    }

    // IMAGE

    private TileStyle parseImage(int size) throws IOException {
        if (!tokenizer.nextLine()) {
            throw error("Incomplete style for size: %d", size);
        }

        String imagePath = nextToken(TokenType.WORD);
        int x = -1;
        int y = -1;

        if (tokenizer.hasNext()) {
            x = parseInt(nextToken(TokenType.WORD));
            y = parseInt(lastWord());
        }

        BufferedImage image = images.get(imagePath);
        if (image == null) {
            image = ImageIO.read(folder.resolve(imagePath).toFile());
            images.put(imagePath, image);
        }

        return new ImageTile(size, image.getSubimage(x, y, size, size));
    }

    private int parseInt(String str) throws IOException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw error(e, "Failed to parse %s to int", str);
        }
    }

    private IOException error(String format, Object... args) {
        return error(tokenizer, format, args);
    }

    private IOException error(Throwable cause, String format, Object... args) {
        return error(tokenizer, cause, format, args);
    }

    private IOException error(LineByLineTokenizer tokenizer, String format, Object... args) {
        return new IOException(format.formatted(args) + " (at " + tokenizer.currentLine() + ")");
    }

    private IOException error(LineByLineTokenizer tokenizer, Throwable cause, String format, Object... args) {
        return new IOException(format.formatted(args) + " (at " + tokenizer.currentLine() + ")", cause);
    }

    // TOKENIZERS

    private static class AnsiTokenizer extends LineByLineTokenizer {

        private int indent = -1;
        private boolean inFunction = false;

        public AnsiTokenizer(BufferedReader br) {
            super(br);
        }

        @Override
        public boolean nextLine() throws IOException {
            line = br.readLine();

            if (line == null) {
                return false;
            } else {
                chars = line.toCharArray();

                if (indent < 0) {
                    indent = getIndent();
                }

                index = indent;

                return true;
            }
        }

        @Override
        protected void fetchNext() {
            if (inFunction) {
                skipWhitespace();

                if (index < chars.length) {
                    char c = chars[index];

                    if (c == ')') {
                        next = new Token(TokenType.FUNC_END, ")");
                        index++;
                        inFunction = false;
                    } else {
                        next = new Token(TokenType.WORD, nextWord(')'));
                    }
                }
            } else {
                StringBuilder sb = new StringBuilder();
                boolean escaped = false;
                for (; index < chars.length; index++) {
                    char c = chars[index];

                    if (escaped) {
                        escaped = false;
                        sb.append(c);
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == '$' && index + 1 < chars.length && chars[index + 1] == '(') {
                        if (sb.length() == 0) {
                            next = new Token(TokenType.FUNC_START, "$(");
                            index += 2;
                            inFunction = true;
                        }

                        break;
                    } else {
                        sb.append(c);
                    }
                }

                if (sb.length() > 0) {
                    next = new Token(TokenType.WORD, sb.toString());
                }
            }
        }

        protected int getIndent() {
            int indent = 0;

            for (; indent < chars.length; indent++) {
                if (!Character.isWhitespace(chars[indent])) {
                    break;
                }
            }

            return indent;
        }

        @Override
        public void reset() {
            index = indent;
        }

        public boolean isInFunction() {
            return inFunction;
        }
    }

    private static class Tokenizer extends LineByLineTokenizer {

        public Tokenizer(BufferedReader br) {
            super(br);
        }

        @Override
        protected void fetchNext() {
            skipWhitespace();

            if (index < chars.length) {
                char c = chars[index];

                if (c == '[') {
                    next = new Token(TokenType.SECTION_START, "[");
                    index++;
                } else if (c == ']') {
                    next = new Token(TokenType.SECTION_END, "]");
                    index++;
                } else if (c == '#') {
                    index = chars.length;
                    next = null;
                } else {
                    next = new Token(TokenType.WORD, nextWord(']'));
                }
            }
        }
    }

    private static abstract class LineByLineTokenizer implements Iterator<Token> {

        protected final BufferedReader br;

        protected String line;
        protected char[] chars;
        protected int index;

        protected Token next;

        public LineByLineTokenizer(BufferedReader br) {
            this.br = br;
        }

        public boolean nextLine() throws IOException {
            line = "";

            while (true) {
                line = br.readLine();

                if (line == null) {
                    return false;
                } else {
                    chars = line.toCharArray();
                    index = 0;

                    for (; index < chars.length; index++) {
                        char c = chars[index];

                        if (c == '#') { // skip if the whole line is commented
                            break;
                        } else if (!Character.isWhitespace(c)) {
                            return true;
                        }
                    }
                }
            }
        }

        protected abstract void fetchNext();

        protected void skipWhitespace() {
            for (; index < chars.length; index++) {
                if (!Character.isWhitespace(chars[index])) {
                    break;
                }
            }
        }

        protected String nextWord(char... stopAt) {
            StringBuilder sb = new StringBuilder();

            char c;
            boolean escaped = false;
            for (; index < chars.length; index++) {
                c = chars[index];

                if (escaped) {
                    escaped = false;
                    sb.append(c);

                } else if (c == '\\') {
                    escaped = true;

                } else if (contains(stopAt, c) || Character.isWhitespace(c)) {
                    break;
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        }

        private boolean contains(char[] array, char c) {
            for (char c2 : array) {
                if (c2 == c) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean hasNext() {
            if (line == null) {
                return false;
            }

            if (next == null) {
                fetchNext();
            }

            return next != null;
        }

        @Override
        public Token next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Token next = this.next;
            this.next = null;

            return next;
        }

        public void reset() {
            index = 0;
        }

        public Token peek() {
            if (!hasNext()) {
                return null;
            }

            return next;
        }

        public String currentLine() {
            return line;
        }
    }

    private static class Token {

        private final TokenType type;
        private final String value;

        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        public TokenType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    private enum TokenType {
        SECTION_START,
        SECTION_END,
        WORD,
        FUNC_START,
        FUNC_END
    }

    @FunctionalInterface
    private interface StyleFunction {

        AttributedStyle apply(AttributedStyle current, LineByLineTokenizer tokenizer) throws IOException;
    }
}
