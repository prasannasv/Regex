package educational.regex.parser;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by prasanna.venkatasubramanian on 9/2/15.
 */
public class Matcher {
    private static final Logger log = Logger.getLogger(Matcher.class.getName());

    private final State start;

    /* package */ Matcher(final State start) {
        this.start = start;
    }

    public boolean matches(final String input) {
        final char[] stream = input.toCharArray();

        final List<State> currentStates = new LinkedList<State>();
        final Set<State> nextStates = new HashSet<State>();
        currentStates.add(start);
        if (start.hasUnlabeledEdges()) {
            currentStates.addAll(start.getNextStates());
        }

        int i;
        for (i = 0; i < stream.length; ++i) {
            final char c = stream[i];
            //log.info("Matching " + c + " at position: " + i + ", with current states: " + currentStates);
            for (final State current : currentStates) {
                if (current.matches(c)) {
                    nextStates.addAll(current.getNextStates());
                }
            }
            //log.info("Next states: " + nextStates);
            currentStates.clear();
            currentStates.addAll(nextStates);
            nextStates.clear();
        }

        return isAnyInTerminalState(currentStates);
    }

    private boolean isAnyInTerminalState(final List<State> states) {
        for (final State state : states) {
            if (state.isTerminal) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return StateSerializer.serialize(start);
    }
}
