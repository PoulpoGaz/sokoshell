package fr.valax.args;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Tokenizer implements Iterator<Token> {

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
        for (Token token : Token.REDIRECT_TOKENS) {

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
                return Token.END_OPTION_TOKEN;

            } else if (Objects.equals(next, '-') && !Objects.equals(nextNext, ' ') && findKeyword(index + 2) == null) {
                // checks for --NOT_KEYWORD

                return Token.OPTION_TOKEN_2;

            } else if (!Objects.equals(next, ' ') && next != null && findKeyword(index + 1) == null) {
                // checks -NOT_KEY_WORD

                return Token.OPTION_TOKEN_1;
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

    public int index() {
        return index;
    }
}
