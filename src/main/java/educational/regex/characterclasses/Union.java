package educational.regex.characterclasses;

/**
 * Created by prasanna.venkatasubramanian on 9/6/15.
 */
class Union implements CharacterClass {
    private final CharacterClass setOne;
    private final CharacterClass setTwo;

    public Union(final CharacterClass setOne, final CharacterClass setTwo) {
        this.setOne = setOne;
        this.setTwo = setTwo;
    }

    @Override
    public boolean isAccepted(final char c) {
        return setOne.isAccepted(c) || setTwo.isAccepted(c);
    }

    @Override
    public String toString() {
        return setOne.toString() + " union " + setTwo.toString();
    }
}
