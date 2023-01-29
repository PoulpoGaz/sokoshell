package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.SokoShell;

import java.io.IOException;
import java.util.*;

/**
 * A collection of {@link SolverParameter} plus the name of the solver used and the level to
 * solve. {@link Solver} known which level to solve thanks to this object
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
        jw.beginObject();
        jw.field("solver", solverName);

        for (Map.Entry<String, SolverParameter> param : parameters.entrySet()) {
            if (param.getValue().hasArgument()) {
                jw.key(param.getKey());
                param.getValue().toJson(jw);
            }
        }

        jw.endObject();
    }

    public static SolverParameters fromJson(IJsonReader jr, Level level) throws JsonException, IOException {
        jr.beginObject();
        String solverName = jr.assertKeyEquals("solver").nextString();

        Solver solver = SokoShell.INSTANCE.getSolver(solverName);
        if (solver == null) {
            throw new IOException("No such solver: " + solverName);
        }

        List<SolverParameter> parameters = solver.getParameters();
        while (!jr.isObjectEnd()) {
            String key = jr.nextKey();

            SolverParameter parameter = parameters.stream()
                    .filter((s) -> s.getName().equals(key))
                    .findFirst()
                    .orElseThrow(() -> new IOException("No such parameter: " + key));

            parameter.fromJson(jr);
        }

        jr.endObject();

        return new SolverParameters(solverName, level, parameters);
    }
}
