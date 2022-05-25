package fr.valax.sokoshell.solver;

import java.util.Collections;
import java.util.List;

public record Pack(String name, String author, List<Level> levels) {

    public Pack(String name, String author, List<Level> levels) {
        this.name = name;
        this.author = author;
        this.levels = Collections.unmodifiableList(levels);
    }
}
