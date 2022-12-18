package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonToken;
import fr.poulpogaz.json.utils.Pair;

import java.io.IOException;
import java.util.*;
import java.util.Map;

/**
 * Contains the level to solve, the type of the solver and various others parameters that are contained in a Map
 * associating the name of the parameter (a string) and the parameter (an object). These parameters can be of any type,
 * but they won't be serialized if the type is not a string, an enum or a number.<br>
 * Some parameters:
 * <ul>
 *     <li>{@link SolverParameters#TIMEOUT}: a period of time after which the solver will stop</li>
 *     <li>{@link SolverParameters#MAX_RAM}: the maximum amount of ram a solver can use</li>
 *     <li>{@link Tracker#TRACKER_PARAM}: a custom {@link Tracker}</li>
 * </ul>
 */
public class SolverParameters {

    private final String solverName;
    private final Level level;
    private final Map<String, SolverParameter> parameters;

    public SolverParameters(String solverName, Level level) {
        this(solverName, level, null);
    }

    public SolverParameters(String solverName, Level level, List<SolverParameter> parameters) {
        this.solverName = Objects.requireNonNull(solverName);
        this.level = Objects.requireNonNull(level);

        if (parameters == null) {
            this.parameters = Map.of();
        } else {
            this.parameters = new HashMap<>();

            for (SolverParameter p : parameters) {
                this.parameters.put(p.getName(), p);
            }
        }
    }

    /**
     * @param param parameter name
     * @return the parameter named param
     */
    public SolverParameter get(String param) {
        return parameters.get(param);
    }

    /**
     *
     * @param param name of the parameter
     * @return argument of parameter param or default value
     * @param <T> type of the argument
     * @throws ClassCastException if the argument can't be cast to a T
     */
    @SuppressWarnings("unchecked")
    public <T> T getArgument(String param) {
        SolverParameter p = parameters.get(param);

        if (p == null) {
            throw new NoSuchElementException("No such parameter: " + param);
        }

        return (T) p.getOrDefault();
    }

    /**
     * @return all parameters
     */
    public Collection<SolverParameter> getParameters() {
        return parameters.values();
    }

    /**
     * @return the level to solve
     */
    public Level getLevel() {
        return level;
    }

    /**
     * @return the name of the solver used
     */
    public String getSolverName() {
        return solverName;
    }


    public void append(IJsonWriter jw) throws JsonException, IOException {
        /*jw.beginObject();
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
        jw.endObject();*/
    }

    public static SolverParameters fromJson(IJsonReader jr, Level level) throws JsonException, IOException {
        /*SolverType solver = null;

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

        return new SolverParameters(solver, level, null);// map);*/
        return null;
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
