package educational.regex.characterclasses;

/**
 * Created by prasanna.venkatasubramanian on 9/6/15.
 */
class Range implements CharacterClass {
    private final char from;
    private final char to;

    public Range(final char from, final char to) throws InvalidCharacterClassSpecification {
        if (from > to) {
            throw new InvalidCharacterClassSpecification();
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean isAccepted(final char c) {
        return from <= c && c <= to;
    }

    @Override
    public String toString() {
        return "[" + from + "-" + to + "]";
    }
}
