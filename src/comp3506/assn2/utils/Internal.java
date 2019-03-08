package comp3506.assn2.utils;

/**
 * An internal node in the trie. Represents one character in a word or indicates the word ends
 */
public class Internal extends Node {

    private Internal preChar;      // the previous character
    private LinkedList children;   // all of the next characters in different words or the word's all occurrences

    /**
     * Constructor. Create an internal node
     *
     * @param letter the character to represent
     * @param preChar the previous character in the word
     */
    public Internal(char letter, Internal preChar) {
        super(letter); // Value: the character. a-z, Apostrophes in the middle of a word, or '\0' for the end
        this.preChar = preChar;
        children = new LinkedList();
    }

    /**
     * @return the children list
     */
    public LinkedList getChildren() {
        return children;
    }

    /**
     * @return the previous character
     */
    public Internal getPreChar() {
        return preChar;
    }

    /**
     * @return the character represented
     */
    public char getChar() {
        return (char)getValue();
    }
}
