package educational.regex.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by prasanna.venkatasubramanian on 9/3/15.
 */
class StateSerializer {

    public static String serialize(final State state) {
        return serialize(state, new HashSet<State>());
    }

    private static String serialize(final State state, final Set<State> visited) {
        if (visited.contains(state)) {
            return " $(" + state.toString() + ") ";
        }
        visited.add(state);
        final StringBuilder sb = new StringBuilder();
        sb.append('{');

        sb.append(state.toString());
        sb.append(", ");

        sb.append(state.isTerminal ? "F, " : "I, ");

        sb.append(state.next != null ? serialize(state.next, visited) : " - ");
        sb.append(", ");

        sb.append(state instanceof ChoiceState ? serialize(((ChoiceState) state).alternative, visited) : " - ");

        sb.append('}');
        return sb.toString();
    }
}
