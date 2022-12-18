package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.commands.AbstractCommand;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SolverParameter {

    protected final String name;
    protected final String description;

    public SolverParameter(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract void set(String argument) throws AbstractCommand.InvalidArgument;

    public abstract Object get();

    public Object getOrDefault() {
        Object o = get();

        if (o == null) {
            o = Objects.requireNonNull(getDefaultValue());
        }

        return o;
    }

    public abstract Object getDefaultValue();

    public boolean hasArgument() {
        return get() != null;
    }


    public void complete(LineReader reader, String argument, List<Candidate> candidates) {

    }

    /**
     * @implNote name is already writen
     */
    public abstract void toJson(IJsonWriter jw) throws JsonException, IOException;

    /**
     * @implNote name is already read
     */
    public abstract void fromJson(IJsonReader jr) throws JsonException, IOException;





    public static class Integer extends SolverParameter {

        protected final int defaultValue;
        protected java.lang.Integer value = null;

        public Integer(String name, int defaultValue) {
            this(name, null, defaultValue);
        }

        public Integer(String name, String description, int defaultValue) {
            super(name, description);
            this.defaultValue = defaultValue;
        }

        @Override
        public void set(String argument) throws AbstractCommand.InvalidArgument {
            try {
                value = java.lang.Integer.parseInt(argument);
            } catch (NumberFormatException e) {
                throw new AbstractCommand.InvalidArgument(e);
            }
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public Object getDefaultValue() {
            return defaultValue;
        }

        @Override
        public void toJson(IJsonWriter jw) throws JsonException, IOException {
            if (value != null) {
                jw.value(value);
            }
        }

        @Override
        public void fromJson(IJsonReader jr) throws JsonException, IOException {
            value = jr.nextInt();
        }
    }

    public static class Long extends SolverParameter {

        protected final long defaultValue;
        protected java.lang.Long value = null;

        public Long(String name, long defaultValue) {
            this(name, null, defaultValue);
        }

        public Long(String name, String description, long defaultValue) {
            super(name, description);
            this.defaultValue = defaultValue;
        }

        @Override
        public void set(String argument) throws AbstractCommand.InvalidArgument {
            try {
                value = java.lang.Long.parseLong(argument);
            } catch (NumberFormatException e) {
                throw new AbstractCommand.InvalidArgument(e);
            }
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public Object getDefaultValue() {
            return defaultValue;
        }

        @Override
        public void toJson(IJsonWriter jw) throws JsonException, IOException {
            if (value != null) {
                jw.value(value);
            }
        }

        @Override
        public void fromJson(IJsonReader jr) throws JsonException, IOException {
            value = jr.nextLong();
        }
    }


    public static class Boolean extends SolverParameter {

        protected final boolean defaultValue;
        protected java.lang.Boolean value = null;

        public Boolean(String name, boolean defaultValue) {
            this(name, null, defaultValue);
        }

        public Boolean(String name, String description, boolean defaultValue) {
            super(name, description);
            this.defaultValue = defaultValue;
        }

        @Override
        public void set(String argument) throws AbstractCommand.InvalidArgument {
            try {
                int v = java.lang.Integer.parseInt(argument);

                value = v != 0;
            } catch (NumberFormatException e) {
                value = java.lang.Boolean.parseBoolean(argument);
            }
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public Object getDefaultValue() {
            return defaultValue;
        }

        @Override
        public void toJson(IJsonWriter jw) throws JsonException, IOException {
            if (value != null) {
                jw.value(value);
            }
        }

        @Override
        public void fromJson(IJsonReader jr) throws JsonException, IOException {
            value = jr.nextBoolean();
        }
    }



    public static class RamParameter extends Long {

        private static final Pattern PATTERN = Pattern.compile("^(\\d+)\\s*([gmk])?b$", Pattern.CASE_INSENSITIVE);

        public RamParameter(String name, long defaultValue) {
            super(name, "Maximal ram usage of the solver", defaultValue);
        }

        public RamParameter(String name, String description, long defaultValue) {
            super(name, description, defaultValue);
        }

        @Override
        public void set(String argument) throws AbstractCommand.InvalidArgument {
            Matcher matcher = PATTERN.matcher(argument);

            if (matcher.matches() && matcher.groupCount() >= 1 && matcher.groupCount() <= 2) {
                long r = java.lang.Long.parseLong(matcher.group(1));

                if (matcher.groupCount() == 2) {
                    String unit = matcher.group(2).toLowerCase();

                    r = switch (unit) {
                        case "g" -> r * 1024 * 1024 * 1024;
                        case "m" -> r * 1024 * 1024;
                        case "k" -> r * 1024;
                        default -> throw new AbstractCommand.InvalidArgument("Invalid ram argument");
                    };
                }

                value = r;
            } else {
                throw new AbstractCommand.InvalidArgument("Invalid ram argument");
            }
        }
    }
}
