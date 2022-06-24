package fr.valax.args;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Tokenizer implements Iterator<Token> {

    private static final String[] keywords = new String[] {
            ">>",
            "2>>",
            ">",
            "2>",
            "&>",
            "2>&1",
            "<<",
            "<",
            "|",
            "&&",
            "||"
    };

    private final char[] chars;
    private int index;

    private Token next;

    public Tokenizer(String text) {
        this.chars = text.toCharArray();
        index = 0;
        next = null;
    }

    private String findKeywordMatching(int index) {
        int maxLength = chars.length - index;

        String bestMatch = null;
        for (String keyword : keywords) {

            if (keyword.length() >= maxLength || (bestMatch != null && bestMatch.length() >= keyword.length())) {
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
                bestMatch = keyword;
            }
        }

        return bestMatch;
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
            } else if (inQuote || c != ' ') {

                String keyword = findKeywordMatching(index);

                if (keyword != null) {
                    if (sb.length() == 0) {
                        index += keyword.length();

                        next = new Token(keyword, true);
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
            next = new Token(sb.toString(), false);
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
