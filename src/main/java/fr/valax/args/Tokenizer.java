package fr.valax.args;

import fr.valax.args.utils.ArgsUtils;

import java.util.NoSuchElementException;
import java.util.Objects;

public class Tokenizer implements ITokenizer {

    private final char[] chars;
    private int index;

    private Token next;

    private boolean userHomeAlias = true;

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

        skipSpace();

        if (index >= chars.length) {
            return;
        }

        char c = chars[index];

        if (c == '-') {
            next = checkForOption(index);
        } else {
            next = findKeyword(index);
        }

        if (next == null) {
            next = getWord();
        } else {
            index += next.value().length();
        }
    }

    private Token getWord() {
        StringBuilder sb = new StringBuilder();

        if (chars[index] == '~' && userHomeAlias) {
            sb.append(ArgsUtils.USER_HOME);
            index++;
        }

        boolean escape = false;
        boolean inQuote = false;
        for (; index < chars.length; index++) {
            char c = chars[index];

            if (escape) {
                sb.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                inQuote = !inQuote;
            } else if (inQuote) {
                sb.append(c);
            } else if (c != ' ') {

                if (findKeyword(index) != null) {
                    break;
                }

                sb.append(c);
            } else {
                break;
            }
        }

        return new Token(sb.toString(), Token.WORD);
    }

    private void skipSpace() {
        for (; index < chars.length; index++) {
            if (chars[index] != ' ') {
                break;
            }
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

    @Override
    public void enableAlias() {
        userHomeAlias = true;
    }

    @Override
    public void disableAlias() {
        userHomeAlias = false;
    }

    public int index() {
        return index;
    }
}
