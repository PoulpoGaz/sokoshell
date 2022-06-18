package fr.valax.sokoshell.solver;

import java.util.Collections;
import java.util.Map;

public class SolverParameters {

    public static final String TIMEOUT = "timeout";
    public static final String MAX_RAM = "max_ram";

    private final Level level;
    private final Map<String, Object> parameters;

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
