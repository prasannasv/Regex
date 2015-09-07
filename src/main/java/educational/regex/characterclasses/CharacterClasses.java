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

    public static CharacterClass anyInRange(final char from, final char to) throws InvalidCharacterClassSpecification {
        if (from > to) {
            throw new InvalidCharacterClassSpecification();
        }
        return new Range(from, to);
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

