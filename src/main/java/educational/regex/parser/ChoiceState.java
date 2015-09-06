package educational.regex.parser;

import java.util.Set;

/**
 * Created by prasanna.venkatasubramanian on 9/2/15.
 */
class ChoiceState extends State {
    final State alternative;

    public ChoiceState(final int id, final State alternative) {
        super(id);
        this.alternative = alternative;
    }

    @Override
    public boolean hasUnlabeledEdges() {
        return true;
    }

    @Override
    public Set<State> getNextStates() {
        final Set<State> nextStates = super.getNextStates();
        nextStates.add(alternative);
        return nextStates;
    }
}
