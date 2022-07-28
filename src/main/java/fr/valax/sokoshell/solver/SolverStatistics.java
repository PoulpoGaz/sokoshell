package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An object that contains various statistics about a solution, including
 * time start and end, number of node explored and queue size every time unit
 */
public class SolverStatistics {

    private final List<InstantStatistic> statistics;

    private long timeStarted; // in millis
    private long timeEnded; // in millis

    public SolverStatistics() {
        statistics = new ArrayList<>();
    }

    public SolverStatistics(List<InstantStatistic> statistics, long timeStarted, long timeEnded) {
        this.statistics = Objects.requireNonNull(statistics);
        this.timeStarted = timeStarted;
        this.timeEnded = timeEnded;
    }

    public void writeStatistics(JsonPrettyWriter jpw) throws JsonException, IOException {
        jpw.beginObject();
        jpw.field("start", timeStarted);
        jpw.field("end", timeEnded);

        jpw.setInline(JsonPrettyWriter.Inline.ARRAY);
        jpw.key("stats").beginArray();

        for (InstantStatistic s : statistics) {
            jpw.beginArray();
            jpw.value(s.time).value(s.nodeExplored).value(s.queueSize);
            jpw.endArray();
        }

        jpw.endArray();
        jpw.setInline(JsonPrettyWriter.Inline.NONE);

        jpw.endObject();
    }

    public static SolverStatistics fromJson(IJsonReader jr) throws JsonException, IOException {
        jr.beginObject();
        long timeStarted = jr.assertKeyEquals("start").nextLong();
        long timeEnded = jr.assertKeyEquals("end").nextLong();

        List<InstantStatistic> statistics = new ArrayList<>();
        jr.assertKeyEquals("stats").beginArray();
        while (!jr.isArrayEnd()) {
            jr.beginArray();

            statistics.add(new InstantStatistic(jr.nextLong(), jr.nextInt(), jr.nextInt()));

            jr.endArray();
        }
        jr.endArray();

        jr.endObject();

        return new SolverStatistics(statistics, timeStarted, timeEnded);
    }

    public void add(int nodeExplored, int queueSize) {
        long time = System.currentTimeMillis();

        statistics.add(new InstantStatistic(time, nodeExplored, queueSize));
    }

    public void setStatistics(List<InstantStatistic> statistics) {
        this.statistics.clear();
        this.statistics.addAll(statistics);
    }

    public List<InstantStatistic> getStatistics() {
        return statistics;
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    public long getTimeEnded() {
        return timeEnded;
    }

    public void setTimeEnded(long timeEnded) {
        this.timeEnded = timeEnded;
    }

    /**
     * Contains statistics at a given instant
     */
    public record InstantStatistic(long time, int nodeExplored, int queueSize) {

    }
}
