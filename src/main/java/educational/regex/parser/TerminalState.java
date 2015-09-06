package educational.regex.parser;

/**
 * Created by prasanna.venkatasubramanian on 9/5/15.
 */
public class TerminalState extends State {
    public TerminalState(final int id) {
        super(id);
        isTerminal = true;
    }
}
