package fr.valax.sokoshell.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class PerformanceMeasurer {

    private static final BigDecimal NANO_TO_MILLIS = BigDecimal.valueOf(1e6);

    private final Map<String, Measurer> measurers = new HashMap<>();

    public void reset() {
        for (Measurer m : measurers.values()) {
            m.reset();
        }
    }

    public void start(String name) {
        Measurer m = measurers.get(name);

        if (m == null) {
            m = new Measurer();
            measurers.put(name, m);
        }

        m.start();
    }

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

    private static class Measurer {

        private BigInteger sum = BigInteger.ZERO;
        private long i;
        private long start;

        public void reset() {
            sum = BigInteger.ZERO;
            i = 0;
            start = 0;
        }

        public void start() {
            start = System.nanoTime();
        }

        public void end() {
            long end = System.nanoTime();

            sum = sum.add(BigInteger.valueOf(end - start));

            i++;
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
