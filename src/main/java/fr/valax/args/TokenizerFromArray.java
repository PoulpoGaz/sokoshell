package fr.valax.args;

import fr.valax.args.utils.ArgsUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * It also assumes that the input was correctly split and won't check for quote.
 * Each string of the array is either: a word, an option + a word or a keyword.
 * String like {@literal <<file.txt} will be considered as a word and not "<<" keyword and "file.txt" word.
 */
public class TokenizerFromArray implements ITokenizer {

    private static final int HYPHEN = 1;
    private static final int DOUBLE_HYPHEN = 2;

    private final String[] args;
    private int index = 0;
    private int inOption = 0;

    private boolean userHomeAlias = true;

    public TokenizerFromArray(String[] args) {
        this.args = args;
    }

    @Override
    public boolean hasNext() {
        return index < args.length || inOption != 0;
    }

    @Override
    public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        String current = args[index];

        if (inOption == DOUBLE_HYPHEN) {
            index++;
            inOption = 0;
            return new Token(current.substring(2), Token.WORD);

        } else if (inOption == HYPHEN) {
            index++;
            inOption = 0;
            return new Token(current.substring(1), Token.WORD);

        } else if (args[index].startsWith("--")) {
            inOption = DOUBLE_HYPHEN;
            return new Token("--", Token.OPTION);

        } else if (args[index].startsWith("-") && args[index].length() > 1) {
            inOption = HYPHEN;
            return new Token("-", Token.OPTION);

        } else {
            Token keyword = findKeyword(current);
            index++;

            if (keyword != null) {
                return keyword;
            } else if (current.startsWith("\\")) {
                return new Token(current.substring(1), Token.WORD);

            } else {
                if (current.startsWith("~") && userHomeAlias) {
                    return new Token(ArgsUtils.USER_HOME + current.substring(1), Token.WORD);
                } else {
                    return new Token(current, Token.WORD);
                }
            }
        }

    }

    @Override
    public void enableAlias() {
        userHomeAlias = true;
    }

    @Override
    public void disableAlias() {
        userHomeAlias = false;
    }

    private Token findKeyword(String str) {
        for (Token t : Token.REDIRECT_TOKENS) {
            if (t.value().equals(str)) {
                return t;
            }
        }

        if (Token.END_OPTION_TOKEN.value().equals(str)) {
            return Token.END_OPTION_TOKEN;
        }

        return null;
    }
}
