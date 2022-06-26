package fr.valax.args;

public record Token(String value, int type) {

    public static final int WORD               = 0;

    // always followed by a word token
    public static final int OPTION             = 1;
    public static final int END_OPTION_PARSING = 2;

    public static final int WRITE_APPEND       = 3;
    public static final int WRITE_ERROR_APPEND = 4;
    public static final int WRITE              = 5;
    public static final int WRITE_ERROR        = 6;
    public static final int BOTH_WRITE         = 7;
    public static final int BOTH_WRITE_APPEND  = 8;
    public static final int STD_ERR_IN_STD_OUT = 9;
    public static final int READ_INPUT_UNTIL   = 10;
    public static final int READ_FILE          = 11;
    public static final int PIPE               = 12;
    public static final int COMMAND_SEPARATOR  = 13;

    public boolean isWord() {
        return type == WORD;
    }

    public boolean isOption() {
        return type == OPTION;
    }

    public boolean isEndOfOption() {
        return type == END_OPTION_PARSING;
    }

    public boolean isRedirect() {
        return type >= WRITE_APPEND && type <= COMMAND_SEPARATOR;
    }
}
