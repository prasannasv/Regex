package educational.regex.characterclasses;

/**
 * Created by prasanna.venkatasubramanian on 9/6/15.
 */
class AnyChar implements CharacterClass {
    @Override
    public boolean isAccepted(final char c) {
        return true;
    }

    @Override
    public String toString() {
        return "ANY";
    }
}
