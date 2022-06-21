package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
            out = Path.of(sourcePath.toString() + ".solutions.json");
        }

        try (BufferedWriter bw = Files.newBufferedWriter(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            IJsonWriter jw = new JsonPrettyWriter(bw);

            jw.beginObject();
            jw.field("pack", name);
            jw.field("author", author);

            for (Level level : levels) {
                if (level.hasSolution()) {

                    jw.key(String.valueOf(level.getIndex()));
                    jw.beginArray();

                    level.writeSolutions(jw);

                    jw.endArray();
                }
            }

            jw.endObject();
            jw.close();
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
