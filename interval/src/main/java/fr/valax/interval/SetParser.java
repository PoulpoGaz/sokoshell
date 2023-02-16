package fr.valax.interval;

/**
 * Allow only positive values.
 *
 * Valid values:
 * 1,2,3-10,4-10s
 *
 */
public class SetParser {

    // start of interval/singleton
    private static final int INTERVAL_SINGLETON_START = 0;

    // after start
    private static final int EXPECT_COMMA_OR_HYPHEN = 1;

    // after hyphen, expecting number
    private static final int INTERVAL_END = 2;

    // after interval end, expecting comma
    private static final int EXPECT_COMMA = 3;

    private SetFactory setFactory = new DefaultFactory();

    private String str;
    private int status;
    private int pos;

    private Set output;
    private float start;

    public SetParser() {

    }

    public Set parse(String str) throws ParseException {
        if (str == null || str.isBlank()) {
            return Empty.INSTANCE;
        }

        this.str = str;
        status = INTERVAL_SINGLETON_START;
        pos = 0;
        output = Empty.INSTANCE;
        start = 0;

        for (; pos < str.length(); pos++) {
            char c = str.charAt(pos);

            if (Character.isWhitespace(c)) {
                continue;
            }

            parseChar(c);
        }

        if (status == EXPECT_COMMA_OR_HYPHEN) {
            if (start == Float.NEGATIVE_INFINITY) {
                throw new RuntimeException();
            } else {
                return output.union(setFactory.singleton(start));
            }

        } else if (status == INTERVAL_END) {
            if (start == Float.NEGATIVE_INFINITY) {
                return setFactory.all();
            } else {
                return output.union(setFactory.greaterThan(start));
            }
        } else if (start == INTERVAL_SINGLETON_START) {
            throw new ParseException("Expecting interval but EOS");
        }

        return output;
    }

    private void parseChar(char c) throws ParseException {
        if (status == INTERVAL_SINGLETON_START) {
            if (c == '-') {
                start = Float.NEGATIVE_INFINITY;
                status = INTERVAL_END;
            } else {
                start = nextNumber();
                status = EXPECT_COMMA_OR_HYPHEN;
            }

        } else if (status == EXPECT_COMMA_OR_HYPHEN) {
            if (c == ',') {
                if (start == Float.NEGATIVE_INFINITY) {
                    throw new RuntimeException();
                } else {
                    output = output.union(setFactory.singleton(start));
                }

                status = INTERVAL_SINGLETON_START;
            } else if (c == '-') {
                status = INTERVAL_END;
            } else {
                throw new ParseException("Expecting a comma or a hyphen");
            }

        } else if (status == INTERVAL_END) {
            if (c == ',') {
                if (start == Float.NEGATIVE_INFINITY) {
                    output = Interval.all();
                } else {
                    output = output.union(setFactory.greaterThan(start));
                }
                status = INTERVAL_SINGLETON_START;
            } else {
                float end = nextNumber();

                if (start == Float.NEGATIVE_INFINITY) {
                    output = output.union(setFactory.lessThan(end));
                } else if (start > end) {
                    throw new ParseException("Invalid range: " + start + "-" + end);
                } else if (start == end) {
                    output = output.union(setFactory.singleton(start));
                } else {
                    output = output.union(setFactory.interval(start, end));
                }

                status = EXPECT_COMMA;
            }

        } else if (status == EXPECT_COMMA) {
            if (c == ',') {
                status = INTERVAL_SINGLETON_START;
            } else {
                throw new ParseException("Expecting comma after interval");
            }

        } else {
            throw new RuntimeException("Unknown state: " + status);
        }
    }

    private float nextNumber() throws ParseException {
        StringBuilder number = new StringBuilder();

        for (; pos < str.length(); pos++) {
            char c = str.charAt(pos);

            if ((c >= '0' && c <= '9') || c == '.') {
                number.append(c);
            } else {
                pos--;
                break;
            }
        }

        try {
            return Float.parseFloat(number.toString());
        } catch (NumberFormatException e) {
            throw new ParseException("Not a number: " + number, e);
        }
    }

    public void setSetFactory(SetFactory setFactory) {
        this.setFactory = setFactory;
    }

    public static class DefaultFactory implements SetFactory {

        @Override
        public Set singleton(float value) {
            return new Singleton(value);
        }

        @Override
        public Set interval(float min, float max) {
            return Interval.closed(min, max);
        }

        @Override
        public Set lessThan(float value) {
            return Interval.lessThan(value);
        }

        @Override
        public Set greaterThan(float value) {
            return Interval.greaterThan(value);
        }

        @Override
        public Set all() {
            return Interval.all();
        }
    }

    public interface SetFactory {

        Set singleton(float value);

        Set interval(float min, float max);

        Set lessThan(float value);

        Set greaterThan(float value);

        Set all();
    }
}
