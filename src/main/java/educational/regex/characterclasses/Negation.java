package educational.regex.characterclasses;

/**
 * Created by prasanna.venkatasubramanian on 9/6/15.
 */
class Negation implements CharacterClass {
    private final CharacterClass characterClass;

    public Negation(final CharacterClass characterClass) {
        this.characterClass = characterClass;
    }

    @Override
    public boolean isAccepted(final char c) {
        return !characterClass.isAccepted(c);
    }

    @Override
    public String toString() {
        return "not " + characterClass.toString();
    }
}
