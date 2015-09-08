# Regex
Simple, Fast Regular Expression Matcher.

This is a very basic regular expression matcher, with super-linear time complexity implementation based on https://swtch.com/~rsc/regexp/regexp1.html, completely in Java.

# Scope
* The metacharacters supported are * + ? ( ) | and \\.
* The precdence of the operators are (from weakest): |, then the implicit concatenation, then * + ?, and at the top ( ).
* Supports simple character classes like ., [a-zA-Z0-9] and so on.
* More fancy character sets like \digit, \D, etc., is in the works.
* Fancy backreferences are not yet supported.
* See ParserTest.java to get a quick sense of possible patterns that are supported.

# Implementation Notes
This is a pure Java implementation. The regular expression is first converted to a postfix notation with '#' used to denote an implicit concatenation operation. The postfix notation is then parsed into an Non-deterministic Finite Automata (NFA), a fancy state machine with each state has at most two branches. The given string is then simulated in the state machine. All possible next states are "walked-on" simultaneously. At the end of the input, if any of the states we are on is a final state, then we consider the regex to match the given input.

# Performance
The runtime is super linear - O(m * n) where m is the length of the input string to match and n is the number of nodes in the NFA. n is linear on the number of characters in the regex. Although we take all possible next steps on an input character, the maximum number of states we would ever be in for the next character is the total number of states, which is not a function of the input string.
