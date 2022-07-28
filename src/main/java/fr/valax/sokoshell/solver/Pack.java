package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.poulpogaz.json.JsonReader;
import jdk.jshell.Snippet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class Pack {

    private static int unnamedIndex = 0;
    private final String name;
    private final String author;
    private final List<Level> levels;

    private Path sourcePath;

    public Pack(String name, String author, List<Level> levels) {
        if (name == null) {
            this.name = "Unnamed[" + unnamedIndex + "]";
            unnamedIndex++;
        } else {
            this.name = name;
        }

        this.author = author;
        this.levels = Collections.unmodifiableList(levels);

        for (Level level : this.levels) {
            level.pack = this;
        }
    }

    public void writeSolutions(Path out) throws IOException, JsonException {
        if (out == null) {
            out = Path.of(sourcePath.toString() + ".solutions.json.gz");
        }

        try (OutputStream os = Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new GZIPOutputStream(os)));

            JsonPrettyWriter jpw = new JsonPrettyWriter(bw);

            jpw.beginObject();
            jpw.field("pack", name);
            jpw.field("author", author);

            for (Level level : levels) {
                if (level.hasSolution()) {

                    jpw.key(String.valueOf(level.getIndex()));
                    jpw.beginArray();

                    level.writeSolutions(jpw);

                    jpw.endArray();
                }
            }

            jpw.endObject();
            jpw.close();
        }
    }

    public void readSolutions(Path in) throws IOException, JsonException {
        if (Files.notExists(in)) {
            return;
        }

        try (InputStream is = Files.newInputStream(in)) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(is)));

            JsonReader jr = new JsonReader(br);

            jr.beginObject();
            String pack = jr.assertKeyEquals("pack").nextString();
            String author = jr.assertKeyEquals("author").nextString();

            if (pack.equals(name) && author.equals(this.author)) {

                while (!jr.isObjectEnd()) {
                    int level = Integer.parseInt(jr.nextKey());

                    Level l = levels.get(level);
                    jr.beginArray();

                    while (!jr.isArrayEnd()) {
                        jr.beginObject();
                        l.addSolution(Solution.fromJson(jr, l));
                        jr.endObject();
                    }

                    jr.endArray();
                }

                jr.endObject();
            }
            jr.close();
        }
    }

    public String name() {
        return name;
    }

    public String author() {
        return author;
    }

    public List<Level> levels() {
        return levels;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(Path sourcePath) {
        this.sourcePath = sourcePath;
    }
}
