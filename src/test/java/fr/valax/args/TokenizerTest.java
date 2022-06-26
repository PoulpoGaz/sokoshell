package fr.valax.args;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static fr.valax.args.Token.*;

public class TokenizerTest {

    private void checkNoKeyword(String[] expected, String str) {
        Tokenizer tokenizer = new Tokenizer(str);

        int i = 0;
        while (tokenizer.hasNext()) {
            Token next = tokenizer.next();
            System.out.println(next);

            Assertions.assertTrue(i < expected.length);
            Assertions.assertEquals(expected[i], next.value());
            Assertions.assertTrue(next.isWord());

            i++;
        }
    }

    @Test
    void test() {
        String[] expected = new String[] {"abc", "def", "ghi", "jkl"};
        String str = "abc   def    ghi    jkl";

        checkNoKeyword(expected, str);
    }

    @Test
    void withQuote() {
        String[] expected = new String[] {"hello", "world", "hello world", "bla", "bla", "bli blohey", "zzz"};

        String str = """
                hello world "hello world" bla bla "bli blo"hey zzz""";

        checkNoKeyword(expected, str);
    }

    @Test
    void escape() {
        String[] expected = new String[] {"hello world", "bliblibloblo"};

        String str = """
                hello\\ world blibli\\bloblo""";

        checkNoKeyword(expected, str);
    }

    @Test
    void escapedAndQuote() {
        String[] expected = new String[] {"\"hello world\"", "!!", "test test test test", "abc", "def"};

        String str = """
                "\\"hello world\\"" !! test\\ "test test"\\ test abc def""";

        checkNoKeyword(expected, str);
    }

    private void assertEquals(Token a, String value, int type) {
        Assertions.assertEquals(value, a.value());
        Assertions.assertEquals(type, a.type());
    }

    @Test
    void keyword() {
        String str = "cat<out  |  sha256sum --help -ggg - -- \\-- \\--bb >>a-test";
        Tokenizer tokenizer = new Tokenizer(str);

        assertEquals(tokenizer.next(), "cat", WORD);
        assertEquals(tokenizer.next(), "<", READ_FILE);
        assertEquals(tokenizer.next(), "out", WORD);
        assertEquals(tokenizer.next(), "|", PIPE);
        assertEquals(tokenizer.next(), "sha256sum", WORD);
        assertEquals(tokenizer.next(), "--", OPTION);
        assertEquals(tokenizer.next(), "help", WORD);
        assertEquals(tokenizer.next(), "-", OPTION);
        assertEquals(tokenizer.next(), "ggg", WORD);
        assertEquals(tokenizer.next(), "-", WORD);
        assertEquals(tokenizer.next(), "--", END_OPTION_PARSING);
        assertEquals(tokenizer.next(), "--", WORD);
        assertEquals(tokenizer.next(), "--bb", WORD);
        assertEquals(tokenizer.next(), ">>", WRITE_APPEND);
        assertEquals(tokenizer.next(), "a-test", WORD);

        Assertions.assertFalse(tokenizer.hasNext());
    }

    @Test
    void endWithOption() {
        String str = "bla -";
        Tokenizer tokenizer = new Tokenizer(str);

        assertEquals(tokenizer.next(), "bla", WORD);
        assertEquals(tokenizer.next(), "-", WORD);

        Assertions.assertFalse(tokenizer.hasNext());

        tokenizer = new Tokenizer("bla --");

        assertEquals(tokenizer.next(), "bla", WORD);
        assertEquals(tokenizer.next(), "--", END_OPTION_PARSING);

        Assertions.assertFalse(tokenizer.hasNext());

        tokenizer = new Tokenizer("bla --<<");

        assertEquals(tokenizer.next(), "bla", WORD);
        assertEquals(tokenizer.next(), "--", END_OPTION_PARSING);
        assertEquals(tokenizer.next(), "<<", READ_INPUT_UNTIL);

        Assertions.assertFalse(tokenizer.hasNext());

        tokenizer = new Tokenizer("bla -<<");

        assertEquals(tokenizer.next(), "bla", WORD);
        assertEquals(tokenizer.next(), "-", WORD);
        assertEquals(tokenizer.next(), "<<", READ_INPUT_UNTIL);

        Assertions.assertFalse(tokenizer.hasNext());

        tokenizer = new Tokenizer("bla -bla --bla");

        assertEquals(tokenizer.next(), "bla", WORD);
        assertEquals(tokenizer.next(), "-", OPTION);
        assertEquals(tokenizer.next(), "bla", WORD);
        assertEquals(tokenizer.next(), "--", OPTION);
        assertEquals(tokenizer.next(), "bla", WORD);

        Assertions.assertFalse(tokenizer.hasNext());
    }
}
