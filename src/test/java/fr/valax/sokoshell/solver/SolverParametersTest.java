package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.*;
import fr.valax.sokoshell.readers.PackReaders;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Map;

public class SolverParametersTest {

    @Test
    void test() throws IOException, JsonException {
        Pack pack = PackReaders.read(Path.of("levels/Original.8xv"), false);

        SolverParameters params = new SolverParameters(SolverType.BFS, pack.levels().get(3), Map.of(
                SolverParameters.TIMEOUT, 1000,
                "test", SolverType.DFS
        ));

        StringWriter sw = new StringWriter();
        IJsonWriter jw = new JsonPrettyWriter(sw);
        params.append(jw);
        jw.close();

        System.out.println(sw);


        IJsonReader jr = new JsonReader(new StringReader(sw.toString()));

        params = SolverParameters.fromJson(jr, pack.levels().get(3));

        jr.close();

        System.out.println(params.getLevel().getIndex());
        System.out.println(params.getSolver());

        params.getParameters().forEach((k, v) -> System.out.println(k + "=" + v));
    }
}
