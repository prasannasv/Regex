package educational.regex.parser;

import educational.regex.ParseException;
import educational.regex.UnexpectedEscapeChar;
import educational.regex.characterclasses.CharacterClasses;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by prasanna.venkatasubramanian on 9/3/15.
 */
class RegexNfa {

    /* package */ static Fragment postfixToNfa(final char[] postfix) throws ParseException {
        final Stack<Fragment> fragmentStack = new Stack<Fragment>();

        int stateId = 0;
        for (int i = 0; i < postfix.length; ++i) {
            final char c = postfix[i];
            Fragment newFragment = null;
            switch (c) {
                case '?': { //choiceState ->(alt) frag.start; newFrag.start <= choiceState;
                    final Fragment poppedFragment = fragmentStack.pop();
                    final State s = new ChoiceState(++stateId, poppedFragment.start);
                    newFragment = new Fragment(s);
                    newFragment.addLeaf(s);
                    newFragment.addLeaves(poppedFragment.leaves);
                    break;
                }
                case '*': { //choiceState ->(alt) frag.start; frag.allLeaves.next <= choiceState;
                    final Fragment poppedFragment = fragmentStack.pop();
                    final State s = new ChoiceState(++stateId, poppedFragment.start);
                    poppedFragment.setAllLeavesNext(s);
                    newFragment = new Fragment(s);
                    newFragment.addLeaf(s);
                    break;
                }
                case '+': { //frag.start.next <= choiceState; choiceState ->(alt) frag.start;
                    final Fragment top = fragmentStack.pop();
                    final State s = new ChoiceState(++stateId, top.start);
                    top.setAllLeavesNext(s);
                    newFragment = new Fragment(top.start);
                    newFragment.addLeaf(s);
                    break;
                }
                case '|': { //choiceState ->(alt) left.start; choiceState ->(next) right.start;
                    final Fragment right = fragmentStack.pop();
                    final Fragment left = fragmentStack.pop();
                    final State s = new ChoiceState(++stateId, left.getStart());
                    s.next = right.getStart();
                    newFragment = new Fragment(s);
                    newFragment.addLeaves(left.leaves);
                    newFragment.addLeaves(right.leaves);
                    break;
                }
                case '\\': {
                    if (i + 1 == postfix.length) {
                        throw new UnexpectedEscapeChar();
                    }
                    final char lookAhead = postfix[++i];
                    final State s = new CharState(++stateId, CharacterClasses.exactMatchOf(lookAhead));
                    newFragment = new Fragment(s);
                    newFragment.addLeaf(s);
                    break;
                }
                case '#': {
                    final Fragment right = fragmentStack.pop();
                    final Fragment left = fragmentStack.pop();
                    left.setAllLeavesNext(right.start);
                    newFragment = new Fragment(left.start);
                    newFragment.addLeaves(right.leaves);
                    break;
                }
                case '.': {
                    final State s = new CharState(++stateId, CharacterClasses.anyCharMatcher());
                    newFragment = new Fragment(s);
                    newFragment.addLeaf(s);
                    break;
                }
                default: {
                    final State s = new CharState(++stateId, CharacterClasses.exactMatchOf(c));
                    newFragment = new Fragment(s);
                    newFragment.addLeaf(s);
                    break;
                }
            }

            //log.info("Pushing new fragment: " + StateSerializer.serialize(newFragment.getStart()));
            fragmentStack.push(newFragment);
        }

        if (fragmentStack.empty()) {
            throw new ParseException();
        }

        final Fragment fragment = fragmentStack.pop();
        final State terminal = new TerminalState(++stateId);
        fragment.setAllLeavesNext(terminal);

        return fragment;
    }

    public static class Fragment {
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
