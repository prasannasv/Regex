package educational.regex.parser;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Created by prasanna.venkatasubramanian on 9/2/15.
 */
public class ParserTest {
    private static final Logger log = Logger.getLogger(ParserTest.class.getName());

    @Before
    public void setUp() {
        log.setLevel(Level.OFF);
    }

    @Test
    public void testInsertionOfConcatOperator() throws Exception {
        final Map<String, String> testCases = new LinkedHashMap<String, String>() {
            {
                put("a", "a");
                put("ab", "a#b");
                put("ab#", "a#b#\\#");
                put("a\\?b", "a#\\?#b");
                put("ab(cd)", "a#b#(c#d)");
                put("ab(c#d)", "a#b#(c#\\##d)");
                put("a(bb)+a", "a#(b#b)+#a");
                put("a(\\*)", "a#(\\*)");
                put("a\\?\\*", "a#\\?#\\*");
                put("a???", "a???");
                put("a?b|c+d*(ef+)?", "a?#b|c+#d*#(e#f+)?");
                put("a\\?b|c+d*(ef+)?", "a#\\?#b|c+#d*#(e#f+)?");
                put("abc", "a#b#c");
                put("a+bc", "a+#b#c");
                put("a|b", "a|b");
                put("a(b)c", "a#(b)#c");
                put("a[b]c", "a#[b]#c");
                put("a[a-z]d", "a#[a-z]#d");
                put("[a-zA-Z0-9]", "[a-z#A-Z#0-9]");
                put("[^a-z]", "[^a-z]");
                //TODO: put("[-0-9]", "[-#0-9]");
                //TODO: put("[0-9-]", "[0-9#-]");
                put("[0-9\\-]", "[0-9#\\-]");
                put("[0-9[a-z]]", "[0-9#[a-z]]");
                put("[0-9\\]]", "[0-9#\\]]");
                put("a#b", "a#\\##b");
                put("a\\*b", "a#\\*#b");
            }
        };
        for (final Map.Entry<String, String> testCase : testCases.entrySet()) {
            assertEquals(testCase.getValue(), new String(Parser.insertConcatOperator(testCase.getKey().toCharArray())));
        }
    }

    @Test
    public void testInfixToPostfix() throws Exception {
        final Map<String, String> testCases = new LinkedHashMap<String, String>() {
            {
                put("a", "a");
                put("ab", "ab#");
                put("a|b", "ab|");
                put("a\\?b", "a\\?#b#");
                put("abcd", "ab#c#d#");
                put("ab(cd)", "ab#cd##");
                put("ab(c#d)", "ab#c\\##d##");
                put("ab#", "ab#\\##");
                put("a???", "a???");
                put("a\\?\\*", "a\\?#\\*#");
                put("a?b|c+d*(ef+)?", "a?b#c+d*#ef+#?#|");
                put("a\\?b|c+d*(ef+)?", "a\\?#b#c+d*#ef+#?#|");
                // With character classes
                put("a.b", "a.#b#");
                put("a[a-z]d", "a[az-]#d#");
                put("[a-zA-Z0-9]", "[az-AZ-#09-#]");
                put("[^a-z]", "[az-^]");
                //TODO: put("[-0-9]", "[-09-#]");
                //TODO: put("[0-9-]", "[09--#]");
                put("[0-9\\-]", "[09-\\-#]");
                put("[0-9[a-z]]", "[09-[az-]#]");
                put("[0-9\\]]", "[09-\\]#]");
            }
        };
        for (final Map.Entry<String, String> testCase : testCases.entrySet()) {
            final String actualOutput = new String(Parser.infixToPostfix(testCase.getKey()));
            assertEquals(testCase.getValue(), actualOutput);
        }
    }

    @Test
    public void testPostfixToNfa() throws Exception {
        final String[] postfixes = {
                "a+",
                "ab#c#",
                "abb#+#a#",
        };
        for (final String postfix : postfixes) {
            final State startState = RegexNfa.postfixToNfa(postfix.toCharArray());
            final String serializedStateTransitions = StateSerializer.serialize(startState);
            log.info("Postfix: " + postfix + " to NFA: " + serializedStateTransitions);
        }
    }

    @Test
    public void testMatch() throws Exception {
        final String[][] testCases = {
                {"a", "a", "true"},
                {"a?", "a", "true"},
                {"a?", "", "true"},
                {"a+", "", "false"},
                {"a*", "aaaaaaa", "true"},
                {"ab", "abc", "false"},
                {"ab*", "a", "true"},
                {"a\\*\\?", "a*?", "true"},
                {"ab*c?", "abbbbb", "true"},
                {"a|b*c?", "bbbbc", "true"},
                {"a(bb)+a", "abbbba", "true"},
                {"a(bb)+a", "abbba", "false"},
                //Character Classes
                {".*", "aaaaaaa", "true"},
                {".", "g", "true"},
                {"a*...", "amen", "true"},
                {"[a-z]", "x", "true"},
                {"a[a-z]d", "and", "true"},
                {"[a-zA-Z0-9]+", "ClassName007", "true"},
                {"[^a-zA-Z0-9]+", "#$!!_+=", "true"},
        };

        for (final String[] testCase : testCases) {
            final String pattern = testCase[0];
            final String input = testCase[1];
            final boolean expectedMatches = Boolean.parseBoolean(testCase[2]);

            log.info("Matching input: " + input + " on pattern: " + pattern);
            final Matcher matcher = Parser.compile(pattern);
            log.info("Compiled " + pattern + " ~> " + matcher);
            final boolean actualMatches = matcher.matches(input);
            assertEquals("pattern: " + pattern + ", input: " + input, expectedMatches, actualMatches);
        }
    }
}
