package fr.valax.sokoshell.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class to hold multiple {@link Measurer}. All measurer have a name and
 * you uee {@link #start(String)} to start the measurer with the specific name.
 */
public class PerformanceMeasurer {

    private static final BigDecimal NANO_TO_MILLIS = BigDecimal.valueOf(1e6);

    private final Map<String, Measurer> measurers = new HashMap<>();

    /**
     * Reset all measurers
     */
    public void reset() {
        for (Measurer m : measurers.values()) {
            m.reset();
        }
    }

    /**
     * Start the measurer named name. If it doesn't exist, it is created
     * .
     * @param name measurer's name
     */
    public void start(String name) {
        Measurer m = measurers.get(name);

        if (m == null) {
            m = new Measurer();
            measurers.put(name, m);
        }

        m.start();
    }

    /**
     * End the measurer named name.
     *
     * @param name measurer's name
     * @throws NullPointerException if there is no measurer with the specified name
     */
    public void end(String name) {
        measurers.get(name).end();
    }

    @Override
    public String toString() {
        if (measurers.isEmpty()) {
            return "nothing has been measured";
        } else {
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, Measurer> entry : measurers.entrySet()) {
                sb.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append('\n');
            }

            return sb.toString();
        }
    }

    /**
     * A measurer is used to calculate the average execution time of a piece of code.
     * The precision of a measurer is the same as the precision of {@link System#nanoTime()}
     * <br>
     * Example:
     * <pre>
     *     Measurer m = new Measurer();
     *
     *     for (int i = 0; i < N; i++) {
     *         m.start();
     *         doStuff();
     *         m.edn();
     *     }
     *
     *     System.out.println(m.averageTime() + " - " + m.numberOfMeasure());
     * </pre>
     */
    public static class Measurer {

        private BigInteger sum = BigInteger.ZERO;
        private long i;
        private long start;

        /**
         * Reset the measurer. After this call {@link #numberOfMeasure()} will return 0 and
         * {@link #averageTime()} -1
         */
        public void reset() {
            sum = BigInteger.ZERO;
            i = 0;
            start = 0;
        }

        /**
         * Start a single measure
         */
        public void start() {
            start = System.nanoTime();
        }

        /**
         * End a single measure
         */
        public void end() {
            long end = System.nanoTime();

            sum = sum.add(BigInteger.valueOf(end - start));

            i++;
        }

        /**
         * Computes and returns the average measured time
         *
         * @return the average time or -1 if no measure has been taken
         */
        public BigDecimal averageTime() {
            if (i == 0) {
                return BigDecimal.valueOf(-1);
            }

            BigDecimal avg = new BigDecimal(sum);
            avg = avg.divide(BigDecimal.valueOf(i).multiply(NANO_TO_MILLIS), 6, RoundingMode.HALF_EVEN);

            return avg;
        }

        /**
         * Returns the number of measure
         *
         * @return the number of measure
         */
        public long numberOfMeasure() {
            return i;
        }

        @Override
        public String toString() {
            if (i == 0) {
                return "nothing has been measured";
            } else {
                BigDecimal avg = new BigDecimal(sum);
                avg = avg.divide(BigDecimal.valueOf(i).multiply(NANO_TO_MILLIS), 6, RoundingMode.HALF_EVEN);

                return "AVG time: " + avg + "ms (measured " + i + " times)";
            }
        }
    }
}
