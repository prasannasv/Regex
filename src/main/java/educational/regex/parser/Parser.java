package educational.regex.parser;

import educational.regex.EmptyRegexException;
import educational.regex.ParseException;
import educational.regex.UnexpectedEscapeChar;
import educational.regex.UnmatchedClosingBrace;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by prasanna.venkatasubramanian on 9/2/15.
 */
public class Parser {
    private static final Logger log = Logger.getLogger(Parser.class.getName());

    static {
        log.setLevel(Level.OFF);
    }

    public static Matcher compile(final String regex) throws ParseException {
        if (regex == null || regex.trim().isEmpty()) {
            throw new EmptyRegexException();
        }
        log.info("infix: " + regex);
        final char[] postfix = infixToPostfix(regex);
        log.info("postfix: " + new String(postfix));
        final RegexNfa.Fragment parsedExpression = RegexNfa.postfixToNfa(postfix);
        return new Matcher(parsedExpression.getStart());
    }

    /* package */ static char[] infixToPostfix(final String regex) throws ParseException {
        final char[] pattern = insertConcatOperator(regex.toCharArray());
        final StringBuilder postfixBuilder = new StringBuilder();

        final Stack<Character> operatorStack = new Stack<Character>();
        boolean isEscaped = false;
        for (int i = 0; i < pattern.length; ++i) {
            final char current = pattern[i];
            if (isEscaped || !isMetaCharacter(current)) {
                isEscaped = false;
                postfixBuilder.append(current);
                continue;
            }

            switch (current) {
                // Unary postfix operators are already in postfix notation. Just add them right away.
                case '+':
                case '?':
                case '*':
                    postfixBuilder.append(current);
                    break;
                // highest precedence operator (. Don't do anything until you find a ).
                case '(':
                    operatorStack.push(current);
                    break;
                case ')':
                    appendUntil('(', operatorStack, postfixBuilder);
                    break;
                case '[':
                    postfixBuilder.append(current);
                    operatorStack.push(current);
                    break;
                case ']':
                    appendUntil('[', operatorStack, postfixBuilder);
                    postfixBuilder.append(current);
                    break;
                case '\\':
                    postfixBuilder.append(current);
                    isEscaped = true;
                    break;
                default:
                    final int precedence = precedence(current);
                    // the = check below makes operators left associative
                    while (!operatorStack.empty() && precedence(operatorStack.peek()) >= precedence) {
                        postfixBuilder.append(operatorStack.pop());
                    }
                    operatorStack.push(current);
                    break;
            }
        }
        while (!operatorStack.empty()) {
            postfixBuilder.append(operatorStack.pop());
        }

        return postfixBuilder.toString().toCharArray();
    }

    /**
     * Lower value implies lower precedence.
     */
    private static int precedence(final char oper) {
        switch (oper) {
            case '(':
                return 1;
            case '[':
                return 2;
            case '|':
                return 3;
            case '#':
                return 4;
            case '^':
                return 5;
            case '-':
                return 6;
        }
        throw new IllegalStateException("Precedence for operator " + oper + " not specified!");
    }

    private static void appendUntil(final char terminal,
                                    final Stack<Character> operatorStack,
                                    final StringBuilder postfixBuilder)
            throws UnmatchedClosingBrace {

        while (true) {
            if (operatorStack.empty()) {
                throw new UnmatchedClosingBrace();
            }
            final char oper = operatorStack.pop();
            if (oper == terminal) {
                //discard the terminal after popping
                break;
            }
            postfixBuilder.append(oper);
        }
    }

    /**
     * This inserts our internal concat operator (#) between tokens.
     * abc -> a#b#c (normal characters are each tokens by default)
     * a+bc -> a+#b#c (normal character followed by a special char like +, ? or * is considered as a single token)
     * a|b -> a|b (| terminates a sequence)
     * a(b)c -> a#(b)#c ('(' and ')' have no meaning separately.)
     * a[b]c -> a#[b]#c (so are '[' and ']')
     * a[a-z]d -> a#[a-z]#d (a-z is a single token)
     * [a-zA-Z0-9] -> [a-z#A-Z#0-9]
     * [^a-z] -> [^a-z]
     * TODO: [-0-9] -> [-#0-9]
     * [0-9\-] -> [0-9#\-]
     * [0-9[a-z]] -> [0-9#[a-z]]
     * [0-9\]] -> [0-9#\]]
     * a#b -> a#\##b (all # in the input should just be escaped)
     * a\*b -> a#\*#b (all escaped meta-characters should be treated as a separate token)
     */
    /* package */ static char[] insertConcatOperator(final char[] pattern) throws UnexpectedEscapeChar {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < pattern.length; ++i) {
            char curr = pattern[i];
            if (curr == '#') {
                sb.append('\\');
            }

            sb.append(curr);

            char lookAhead = lookAhead(pattern, i);
            if (curr == '\\') {
                if (lookAhead == '#' || !isMetaCharacter(lookAhead)) {
                    throw new UnexpectedEscapeChar();
                }
                sb.append(lookAhead);
                ++i;
                curr = lookAhead;
                lookAhead = lookAhead(pattern, i);
            }

            if (i < pattern.length - 1 &&
                    !isSequenceBeginner(curr) &&
                    !isUnaryPrefixOperator(curr) &&
                    !isBinaryOperator(curr) &&
                    (!shouldSkipConcatOper(lookAhead) || curr == '\\')) {
                sb.append('#');
            }
        }

        return sb.toString().toCharArray();
    }

    private static char lookAhead(final char[] pattern, final int i) {
        return (i < pattern.length - 1) ? pattern[i + 1] : '\0';
    }

    private static boolean isUnaryOperator(final char c) {
        return isUnaryPostfixOperator(c) || isUnaryPrefixOperator(c);
    }

    private static boolean isUnaryPostfixOperator(final char c) {
        return c == '+' || c == '?' || c == '*';
    }

    private static boolean isUnaryPrefixOperator(final char c) {
        return c == '^';
    }

    private static boolean isBinaryOperator(final char c) {
        return c == '|' || c == '-';  // '#' is an "internal" binary operator.
    }

    private static boolean isSequenceBeginner(final char c) {
        return c == '(' || c == '[' || c == '|';
    }

    private static boolean isSequenceTerminator(final char c) {
        return c == ')' || c == ']' || c == '|';
    }

    private static boolean isSequenceDelimiter(final char c) {
        return isSequenceTerminator(c) || isSequenceBeginner(c);
    }

    private static boolean shouldSkipConcatOper(final char c) {
        return isSequenceTerminator(c) || isUnaryPostfixOperator(c) || isBinaryOperator(c);
    }

    private static boolean isMetaCharacter(final char c) {
        return isUnaryOperator(c) || isSequenceDelimiter(c) || isBinaryOperator(c) || c == '\\' || c == '#';
    }

    private static void popConcatOperIfPresent(final StringBuilder postfixBuilder,
                                               final Stack<Character> operatorStack) {
        if (!operatorStack.empty() && operatorStack.peek() == '#') {
            postfixBuilder.append(operatorStack.pop());
        }
    }
}
