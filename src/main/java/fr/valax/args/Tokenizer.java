package fr.valax.args;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Tokenizer implements Iterator<Token> {

    private static final Token[] redirectTokens = new Token[] {
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
    private static final Token OPTION_TOKEN_1 = new Token("-", Token.OPTION);

    // expect a char directly after
    private static final Token OPTION_TOKEN_2 = new Token("--", Token.OPTION);

    // expect a space directly after
    private static final Token END_OPTION_TOKEN = new Token("--", Token.END_OPTION_PARSING);

    private final char[] chars;
    private int index;

    private Token next;

    public Tokenizer(String text) {
        this.chars = text.toCharArray();
        index = 0;
        next = null;
    }

    private Token findKeyword(int index) {
        int maxLength = chars.length - index;

        if (maxLength <= 0) {
            return null;
        }

        Token bestMatch = null;
        for (Token token : redirectTokens) {

            String keyword = token.value();

            if (keyword.length() > maxLength || (bestMatch != null && bestMatch.value().length() > keyword.length())) {
                continue;
            }

            boolean match = true;
            for (int i = index, j = 0; j < keyword.length(); j++, i++) {
                if (chars[i] != keyword.charAt(j)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                bestMatch = token;
            }
        }

        return bestMatch;
    }

    private Token checkForOption(int index) {
        int maxIndex = chars.length - index - 1;

        char current = chars[index];

        if (current == '-') {
            Character next = maxIndex > 0 ? chars[index + 1] : null;
            Character nextNext = maxIndex > 1 ? chars[index + 2] : null;

            // checks for --SPACE or --END or --KEYWORD
            if (Objects.equals(next, '-') &&
                    (Objects.equals(nextNext, ' ') || nextNext == null || findKeyword(index + 2) != null)) {
                return END_OPTION_TOKEN;

            } else if (Objects.equals(next, '-') && !Objects.equals(nextNext, ' ') && findKeyword(index + 2) == null) {
                // checks for --NOT_KEYWORD

                return OPTION_TOKEN_2;

            } else if (!Objects.equals(next, ' ') && next != null && findKeyword(index + 1) == null) {
                // checks -NOT_KEY_WORD

                return OPTION_TOKEN_1;
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    private void fetchNextIfNeeded() {
        if (next != null) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        boolean escape = false;
        boolean inQuote = false;
        for (; index < chars.length; index++) {
            char c = chars[index];

            boolean escape2 = escape;
            escape = false;

            if (escape2) {
                sb.append(c);

            } else if (c == '\\') {
                escape = true;

            } else if (c == '"') {
                inQuote = !inQuote;

            } else if (inQuote) {
                sb.append(c);

            } else if (c != ' ') {
                Token keyword;

                if (c == '-' && sb.length() == 0) {
                    keyword = checkForOption(index);
                } else {
                    keyword = findKeyword(index);
                }

                if (keyword != null) {
                    if (sb.length() == 0) {
                        index += keyword.value().length();

                        next = keyword;
                    }

                    break;
                } else {
                    sb.append(c);
                }
            } else if (sb.length() > 0) {
                index++;
                break;
            }
        }

        if (sb.length() > 0) {
            next = new Token(sb.toString(), Token.WORD);
        }
    }

    @Override
    public boolean hasNext() {
        fetchNextIfNeeded();
        return next != null;
    }

    @Override
    public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Token next = this.next;
        this.next = null;

        return next;
    }

    public Token peek() {
        if (!hasNext()) {
            return null;
        } else {
            return next;
        }
    }
}
