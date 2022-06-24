package fr.valax.args;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenizerTest {

    private void checkNoKeyword(String[] expected, String str) {
        Tokenizer tokenizer = new Tokenizer(str);

        int i = 0;
        while (tokenizer.hasNext()) {
            Token next = tokenizer.next();
            System.out.println(next);

            Assertions.assertTrue(i < expected.length);
            Assertions.assertEquals(expected[i], next.value());
            Assertions.assertFalse(next.keyword());

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

    private void assertEquals(Token a, String value, boolean keyword) {
        Assertions.assertEquals(a.value(), value);
        Assertions.assertEquals(a.keyword(), keyword);
    }

    @Test
    void keyword() {
        String str = "cat<out  |  sha256sum";
        Tokenizer tokenizer = new Tokenizer(str);

        assertEquals(tokenizer.next(), "cat", false);
        assertEquals(tokenizer.next(), "<", true);
        assertEquals(tokenizer.next(), "out", false);
        assertEquals(tokenizer.next(), "|", true);
        assertEquals(tokenizer.next(), "sha256sum", false);
        Assertions.assertFalse(tokenizer.hasNext());
    }
}
