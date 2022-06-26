package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonToken;
import fr.poulpogaz.json.utils.Pair;
import fr.valax.sokoshell.solver.tasks.SolverTask;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Solver parameters.
 * Some parameters (tracker) are used by {@link SolverTask}.
 * Using them with only a {@link Solver} has no effect.
 */
public class SolverParameters {

    public static final String TIMEOUT = "timeout";
    public static final String MAX_RAM = "max_ram";

    private final SolverType solver;
    private final Level level;
    private final Map<String, Object> parameters;

    public SolverParameters(SolverType solver, Level level) {
        this(solver, level, Map.of());
    }

    public SolverParameters(SolverType solver, Level level, Map<String, Object> parameters) {
        this.solver = solver;
        this.level = level;
        this.parameters = Collections.unmodifiableMap(parameters);

        for (Object o : parameters.values()) {
            if (!(o instanceof Enum<?>) && !(o instanceof String) && !(o instanceof Number)) {
                throw new IllegalArgumentException("Invalid object class: " + o.getClass());
            }
        }
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

    public SolverType getSolver() {
        return solver;
    }

    public void append(IJsonWriter jw) throws JsonException, IOException {
        jw.beginObject();
        jw.field("solver", solver.name());

        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            jw.key(param.getKey());

            if (param.getValue() instanceof Enum<?> e) {
                jw.beginArray();
                jw.value(e.getClass().getName());
                jw.value(e.name());
                jw.endArray();
            } else if (param.getValue() instanceof String str) {
                jw.value(str);
            } else if (param.getValue() instanceof Number e) {
                jw.value(e);
            }
        }
        jw.endObject();
    }

    public static SolverParameters fromJson(IJsonReader jr, Level level) throws JsonException, IOException {
        SolverType solver = null;

        Map<String, Object> map = new HashMap<>();

        jr.beginObject();

        while (!jr.isObjectEnd()) {
            String key = jr.nextKey();

            if ("solver".equals(key)) {
                solver = SolverType.valueOf(jr.nextString());
            } else {
                parseParameter(key, jr, map);
            }
        }

        jr.endObject();

        if (solver == null) {
            throw new IOException("Can't find solver");
        }

        return new SolverParameters(solver, level, map);
    }

    private static void parseParameter(String key, IJsonReader jr, Map<String, Object> parameters) throws JsonException, IOException {
        Pair<JsonToken, Object> pair = jr.next();
        JsonToken token = pair.getLeft();

        if (token.isNumber() || token == JsonToken.STRING_TOKEN) {
            parameters.put(key, pair.getRight());

        } else if (token == JsonToken.BEGIN_ARRAY_TOKEN) {

            String enumName = jr.nextString();
            String name = jr.nextString();

            jr.endArray();

            try {
                Class<?> cls = Class.forName(enumName);

                if (!cls.isEnum()) {
                    throw new IOException("Not an enum");
                }

                parameters.put(key, getEnum(cls, name));
            } catch (ClassNotFoundException e) {
                throw new IOException("Invalid class: " + enumName, e);
            }

        } else {
            throw new IOException("Invalid parameter: " + key);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> T getEnum(Class<?> cls, String name) {
        if (cls.isEnum()) {
            Class<T> clsEnum = (Class<T>) cls;

            return Enum.valueOf(clsEnum, name);
        } else {
            return null;
        }
    }
}
