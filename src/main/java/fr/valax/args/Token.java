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

    public static final Token[] REDIRECT_TOKENS = new Token[] {
            new Token(">>",   Token.WRITE_APPEND),
            new Token("2>>",  Token.WRITE_ERROR_APPEND),
            new Token(">",    Token.WRITE),
            new Token("2>",   Token.WRITE_ERROR),
            new Token("&>",   Token.BOTH_WRITE),
            new Token("&>>",  Token.BOTH_WRITE_APPEND),
            new Token("2>&1", Token.STD_ERR_IN_STD_OUT),
            new Token("<<",   Token.READ_INPUT_UNTIL),
            new Token("<",    Token.READ_FILE),
            new Token("|",    Token.PIPE),
            new Token(";",    Token.COMMAND_SEPARATOR)
    };

    // expect a char directly after
    public static final Token OPTION_TOKEN_1 = new Token("-", Token.OPTION);

    // expect a char directly after
    public static final Token OPTION_TOKEN_2 = new Token("--", Token.OPTION);

    // expect a space directly after
    public static final Token END_OPTION_TOKEN = new Token("--", Token.END_OPTION_PARSING);

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
