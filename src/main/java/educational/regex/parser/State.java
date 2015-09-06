package educational.regex.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by prasanna.venkatasubramanian on 9/2/15.
 */
class State {
    protected final int id;
    protected State next;
    protected boolean isTerminal;

    public State(final int id) {
        this.id = id;
    }

    public boolean hasUnlabeledEdges() {
        return false;
    }

    /**
     * For any state that doesn't have a labeled edge, we treat it as a non-match.
     * The epsilon transitions are taken care of when we walk the state machine.
     */
    public boolean matches(final char c) {
        return false;
    }

    public Set<State> getNextStates() {
        return new HashSet<State>() {
            {
                if (next != null) {
                    add(next);
                    if (next.hasUnlabeledEdges()) {
                        addAll(next.getNextStates());
                    }
                }
            }
        };
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
