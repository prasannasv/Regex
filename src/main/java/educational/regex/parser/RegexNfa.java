package educational.regex.parser;

import educational.regex.ParseException;
import educational.regex.UnexpectedEscapeChar;
import educational.regex.UnmatchedClosingBrace;
import educational.regex.characterclasses.CharacterClass;
import educational.regex.characterclasses.CharacterClasses;
import educational.regex.characterclasses.InvalidCharacterClassSpecification;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by prasanna.venkatasubramanian on 9/3/15.
 */
class RegexNfa {
    private final Stack<Fragment> fragmentStack = new Stack<Fragment>();

    private int stateId = 0;

    /* package */ static State postfixToNfa(final char[] postfix) throws ParseException {
        return new RegexNfa().createNfa(postfix).getStart();
    }

    private Fragment createNfa(final char[] postfix) throws ParseException {
        int numCharacterClassOpens = 0;
        for (int i = 0; i < postfix.length; ++i) {
            Fragment newFragment = null;
            final char c = postfix[i];
            switch (c) {
                case '?':
                    newFragment = handleZeroOrOne();
                    break;
                case '*':
                    newFragment = handleZeroOrMore();
                    break;
                case '+':
                    newFragment = handleOneOrMore();
                    break;
                case '|':
                    newFragment = handleAlternation();
                    break;
                case '#':
                    newFragment = numCharacterClassOpens > 0 ? handleUnion() : handleConcatenation();
                    break;
                case '\\':
                    if (i + 1 == postfix.length) {
                        throw new UnexpectedEscapeChar();
                    }
                    newFragment = handleCharacterClass(CharacterClasses.exactMatchOf(postfix[++i]));
                    break;
                case '[':
                    ++numCharacterClassOpens;
                    break;
                case ']':
                    --numCharacterClassOpens;
                    if (numCharacterClassOpens < 0) {
                        throw new UnmatchedClosingBrace();
                    }
                    break;
                case '^':
                    newFragment = numCharacterClassOpens > 0 ? handleNegation() :
                            handleCharacterClass(CharacterClasses.exactMatchOf(c));
                    break;
                case '-':
                    newFragment = numCharacterClassOpens > 0 ? handleRange() :
                            handleCharacterClass(CharacterClasses.exactMatchOf(c));
                    break;
                case '.':
                    newFragment = handleCharacterClass(CharacterClasses.anyCharMatcher());
                    break;
                default:
                    newFragment = handleCharacterClass(CharacterClasses.exactMatchOf(c));
                    break;
            }

            if (newFragment != null) {
                fragmentStack.push(newFragment);
            }
        }

        if (fragmentStack.empty()) {
            throw new ParseException();
        }

        final Fragment fragment = fragmentStack.pop();
        final State terminal = new TerminalState(++stateId);
        fragment.setAllLeavesNext(terminal);

        return fragment;
    }

    /**
     * New State S that has a labeled dangling edge. The label matches a char
     * as specified by the passed in characterClass.
     */
    private Fragment handleCharacterClass(final CharacterClass characterClass) {
        final State s = new CharState(++stateId, characterClass);

        final Fragment newFragment = new Fragment(s);
        newFragment.addLeaf(s);

        return newFragment;
    }

    /**
     * Pop fragment [RE] from stack.
     *
     * Create new State S.
     * Set transition on lambda1 from S to [RE]'s start.
     * Let transition on lambda2 from S be dangling.
     *
     * Create new fragment with start state as S.
     * Make all unassigned transitions as leaves of the new fragment.
     */
    private Fragment handleZeroOrOne() {
        final Fragment top = fragmentStack.pop();
        final State s = new ChoiceState(++stateId, top.start);

        final Fragment newFragment = new Fragment(s);
        newFragment.addLeaf(s);
        newFragment.addLeaves(top.leaves);

        return newFragment;
    }

    /**
     * Pop fragment [RE] from stack.
     *
     * Create new State S.
     * Set transition on lambda1 from S to [RE]'s start.
     * Let transition on lambda2 from S be dangling.
     * Set all dangling transitions of [RE] to S.
     *
     * Create new fragment with start state as S.
     * Make all unassigned transitions as leaves of the new fragment.
     */
    private Fragment handleZeroOrMore() {
        final Fragment top = fragmentStack.pop();
        final State s = new ChoiceState(++stateId, top.start);
        top.setAllLeavesNext(s);

        final Fragment newFragment = new Fragment(s);
        newFragment.addLeaf(s);

        return newFragment;
    }

    /**
     * Pop fragment [RE] from stack.
     *
     * Create new State S.
     * Set transition on lambda1 from S to [RE]'s start.
     * Let transition on lambda2 from S be dangling.
     * Set all dangling transitions of [RE] to S.
     *
     * Create new fragment with start state as [RE]'s start. (This is the only difference between a * and +).
     * Make all unassigned transitions as leaves of the new fragment.
     */
    private Fragment handleOneOrMore() {
        final Fragment top = fragmentStack.pop();
        final State s = new ChoiceState(++stateId, top.start);
        top.setAllLeavesNext(s);

        final Fragment newFragment = new Fragment(top.start);
        newFragment.addLeaf(s);

        return newFragment;
    }

    /**
     * Pop two [RE]'s from stack.
     *
     * Create a new State S.
     * Set transition on lambda1 from S to [RE1]'s start.
     * Set transition on lambda2 from S to [RE2]'s start.
     *
     * Create a new fragment with this new state S.
     * Mark all unassigned transitions as leaves of the new fragment.
     */
    private Fragment handleAlternation() {
        final Fragment right = fragmentStack.pop();
        final Fragment left = fragmentStack.pop();
        final State s = new ChoiceState(++stateId, left.getStart());
        s.next = right.getStart();

        final Fragment newFragment = new Fragment(s);
        newFragment.addLeaves(left.leaves);
        newFragment.addLeaves(right.leaves);

        return newFragment;
    }

    /**
     * Pop two [RE]'s from stack.
     *
     * Set transition of all leaves of [RE1] to [RE2]'s start state.
     *
     * Create a new fragment with [RE1]'s start state.
     * Mark all unassigned transitions as leaves of the new fragment.
     */
    private Fragment handleConcatenation() {
        final Fragment right = fragmentStack.pop();
        final Fragment left = fragmentStack.pop();
        left.setAllLeavesNext(right.start);

        final Fragment newFragment = new Fragment(left.start);
        newFragment.addLeaves(right.leaves);

        return newFragment;
    }

    private Fragment handleUnion() throws ParseException {
        final Fragment right = fragmentStack.pop();
        final Fragment left = fragmentStack.pop();

        if (!(left.getStart() instanceof CharState && right.getStart() instanceof CharState)) {
            throw new InvalidCharacterClassSpecification();
        }
        final CharacterClass leftCharClass = ((CharState) left.getStart()).getAcceptableSet();
        final CharacterClass rightCharClass = ((CharState) right.getStart()).getAcceptableSet();

        final State s = new CharState(++stateId, CharacterClasses.union(leftCharClass, rightCharClass));
        final Fragment newFragment = new Fragment(s);
        newFragment.addLeaf(s);

        return newFragment;
    }

    private Fragment handleNegation() throws ParseException {
        final Fragment top = fragmentStack.pop();
        if (!(top.getStart() instanceof CharState)) {
            throw new InvalidCharacterClassSpecification();
        }

        final CharacterClass characterClass = ((CharState) top.getStart()).getAcceptableSet();

        final State s = new CharState(++stateId, CharacterClasses.negationOf(characterClass));
        final Fragment newFragment = new Fragment(s);
        newFragment.addLeaf(s);

        return newFragment;
    }

    private Fragment handleRange() throws ParseException {
        final Fragment right = fragmentStack.pop();
        final Fragment left = fragmentStack.pop();

        if (!(left.getStart() instanceof CharState && right.getStart() instanceof CharState)) {
            throw new InvalidCharacterClassSpecification();
        }
        final CharacterClass leftCharClass = ((CharState) left.getStart()).getAcceptableSet();
        final CharacterClass rightCharClass = ((CharState) right.getStart()).getAcceptableSet();

        final State s = new CharState(++stateId, CharacterClasses.anyInRange(leftCharClass, rightCharClass));
        final Fragment newFragment = new Fragment(s);
        newFragment.addLeaf(s);

        return newFragment;
    }

    private static class Fragment {
        private State start;
        private Set<State> leaves = new HashSet<State>();

        public Fragment(final State start) {
            this.start = start;
        }

        public State getStart() {
            return start;
        }

        public void addLeaf(final State state) {
            leaves.add(state);
        }

        public void addLeaves(final Set<State> leaves) {
            this.leaves.addAll(leaves);
        }

        public void setAllLeavesNext(final State s) {
            for (final State leaf : leaves) {
                leaf.next = s;
            }
        }

        @Override
        public String toString() {
            return "start: " + start + ", leaves: " + leaves;
        }
    }
}
