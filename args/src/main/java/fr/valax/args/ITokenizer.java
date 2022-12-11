package fr.valax.args;

import java.util.Iterator;

public interface ITokenizer extends Iterator<Token> {

    @Override
    boolean hasNext();

    @Override
    Token next();

    void enableAlias();

    void disableAlias();
}
