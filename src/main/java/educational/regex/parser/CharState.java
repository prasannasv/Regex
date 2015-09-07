package educational.regex.parser;

import educational.regex.characterclasses.CharacterClass;

/**
 * Created by prasanna.venkatasubramanian on 9/2/15.
 */
class CharState extends State {
    private final CharacterClass acceptableSet;

    CharState(final int id, final CharacterClass acceptableSet) {
        super(id);
        this.acceptableSet = acceptableSet;
    }

    @Override
    public boolean matches(final char c) {
        return acceptableSet.isAccepted(c);
    }

    @Override
    public String toString() {
        return id + ":" + acceptableSet.toString();
    }
}
