package educational.regex.characterclasses;

/**
 * Created by prasanna.venkatasubramanian on 9/6/15.
 */
public class CharacterClasses {

    public static CharacterClass exactMatchOf(final char c) {
        return new SingleChar(c);
    }

    public static CharacterClass anyCharMatcher() {
        return new AnyChar();
    }

    public static CharacterClass anyInRange(final CharacterClass from,
                                            final CharacterClass to)
            throws InvalidCharacterClassSpecification {
        if (!(from instanceof SingleChar && to instanceof SingleChar)) {
            throw new InvalidCharacterClassSpecification();
        }

        return new Range(((SingleChar) from).getAcceptedChar(), ((SingleChar) to).getAcceptedChar());
    }

    public static CharacterClass negationOf(final CharacterClass characterClass) {
        return new Negation(characterClass);
    }

    public static CharacterClass union(final CharacterClass setOne, final CharacterClass setTwo) {
        return new Union(setOne, setTwo);
    }

    public static CharacterClass intersection(final CharacterClass setOne, final CharacterClass setTwo) {
        return new Intersection(setOne, setTwo);
    }
}

