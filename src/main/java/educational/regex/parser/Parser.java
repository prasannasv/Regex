package educational.regex.parser;

import educational.regex.EmptyRegexException;
import educational.regex.ParseException;
import educational.regex.UnexpectedEscapeChar;
import educational.regex.UnmatchedClosingBrace;

import java.util.Stack;

/**
 * Created by prasanna.venkatasubramanian on 9/2/15.
 */
public class Parser {
    public static Matcher compile(final String regex) throws ParseException {
        if (regex == null || regex.trim().isEmpty()) {
            throw new EmptyRegexException();
        }
        final char[] postfix = infixToPostfix(regex);
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
                // unary operators, add them right away
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
                    while (true) {
                        if (operatorStack.empty()) {
                            throw new UnmatchedClosingBrace();
                        }
                        final char oper = operatorStack.pop();
                        if (oper == '(') {
                            break;
                        }
                        postfixBuilder.append(oper);
                    }
                    break;
                case '\\':
                    postfixBuilder.append(current);
                    isEscaped = true;
                    break;
                default:
                    popConcatOperIfPresent(postfixBuilder, operatorStack);
                    operatorStack.push(current);
                    break;
            }
        }
        while (!operatorStack.empty()) {
            postfixBuilder.append(operatorStack.pop());
        }

        return postfixBuilder.toString().toCharArray();
    }

    /* package */ static char[] insertConcatOperator(final char[] pattern) throws UnexpectedEscapeChar {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < pattern.length; ++i) {
            final char curr = pattern[i];
            if (curr == '#') {
                sb.append('\\');
            }

            sb.append(curr);

            final char lookAhead = (i < pattern.length - 1) ? pattern[i + 1] : '\0';
            if (curr == '\\') {
                if (lookAhead == '#' || !isMetaCharacter(lookAhead)) {
                    throw new UnexpectedEscapeChar();
                }
                sb.append(lookAhead);
                ++i;
            }

            if (curr != '|' && curr != '(' && (!shouldSkipConcatOper(lookAhead) || curr == '\\')) {
                sb.append('#');
            }
        }

        return sb.toString().toCharArray();
    }

    private static boolean shouldSkipConcatOper(final char c) {
        return c == ')' || c == '|' || c == '*' || c == '?' || c =='+' || c == '\0';
    }

    private static boolean isMetaCharacter(final char c) {
        return c == '?' || c == '+' || c == '*' || c == '(' || c == ')' || c == '\\' || c == '#' || c == '|';
    }

    private static void popConcatOperIfPresent(final StringBuilder postfixBuilder,
                                               final Stack<Character> operatorStack) {
        if (!operatorStack.empty() && operatorStack.peek() == '#') {
            postfixBuilder.append(operatorStack.pop());
        }
    }
}
