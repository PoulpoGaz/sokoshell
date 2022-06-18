package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.tasks.SolverTask;

import java.util.Collections;
import java.util.Map;

/**
 * Solver parameters.
 * Some parameters (tracker) are used by {@link SolverTask}.
 * Using them with only a {@link Solver} has no effect.
 */
public class SolverParameters {

    public static final String TIMEOUT = "timeout";
    public static final String MAX_RAM = "max_ram";

    private final Level level;
    private final Map<String, Object> parameters;

    public SolverParameters(Level level) {
        this(level, Map.of());
    }

    public SolverParameters(Level level, Map<String, Object> parameters) {
        this.level = level;
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public Object get(String key) {
        return parameters.get(key);
    }

    public Object getOrDefault(String key, Object defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Level getLevel() {
        return level;
    }
}
