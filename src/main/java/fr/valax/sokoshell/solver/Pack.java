package fr.valax.sokoshell.solver;

import java.util.Collections;
import java.util.List;

public record Pack(String name, String author, List<Level> levels) {

    private static int unnamedIndex = 0;

    public Pack(String name, String author, List<Level> levels) {
        if (name == null) {
            this.name = "Unnamed[" + unnamedIndex + "]";
            unnamedIndex++;
        } else {
            this.name = name;
        }

        this.author = author;
        this.levels = Collections.unmodifiableList(levels);
    }
}
