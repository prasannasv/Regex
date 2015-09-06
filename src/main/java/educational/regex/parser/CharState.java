package educational.regex.parser;

/**
 * Created by prasanna.venkatasubramanian on 9/2/15.
 */
class CharState extends State {
    final char label;

    CharState(final int id, final char label) {
        super(id);
        this.label = label;
    }

    @Override
    public boolean matches(final char c) {
        return c == label;
    }

    @Override
    public String toString() {
        return id + ":" + label;
    }
}
