package educational.regex.characterclasses;

/**
 * Created by prasanna.venkatasubramanian on 9/6/15.
 */
class SingleChar implements CharacterClass {
    private final char label;

    public SingleChar(final char label) {
        this.label = label;
    }

    @Override
    public boolean isAccepted(final char c) {
        return label == c;
    }

    @Override
    public String toString() {
        return String.valueOf(label);
    }
}
