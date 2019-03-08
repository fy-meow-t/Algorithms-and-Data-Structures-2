package comp3506.assn2.utils;

/**
 * A leaf node in the trie.
 * Contains the word's position (line and column number), the last character, and the next word's leaf node
 *
 * If in an index-trie storing titles and their line numbers,
 * "line" is the first line while "col" is the last line of the section. "nextWord" and "lastChar" are null
 */
public class Leaf extends Node {

    private int col;            // the word's column number
    private Leaf nextWord;      // the next word in the searched text
    private Internal lastChar;  // the last character of the word

    /**
     * Constructor. Create a new leaf
     *
     * @param line the word's line number
     * @param col  the column number
     */
    public Leaf(int line, int col) {
        super(line);
        this.col = col;
        nextWord = null;
        lastChar = null;
    }

    /**
     * @return the line number of the word or the starting line of the section
     */
    public int getLine() {
        return (int) getValue();
    }

    /**
     * @return the column number of the word or the end line of the section
     */
    public int getCol() {
        return col;
    }

    /**
     * @param col column number or the last line of a section
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * @return the next word's lead node
     */
    public Leaf getNextWord() {
        return nextWord;
    }

    /**
     * @param nextWord the next word's leaf node to link to
     */
    public void setNextWord(Leaf nextWord) {
        this.nextWord = nextWord;
    }

    /**
     * @return the internal node that represents the last character of the word
     */
    public Internal getLastChar() {
        return lastChar;
    }

    /**
     * @param lastChar the last character's node to link to
     */
    public void setLastChar(Internal lastChar) {
        this.lastChar = lastChar;
    }

    /**
     * @return a new leaf node that copies the line and column number
     */
    @Override
    public Leaf copy() {
        return new Leaf((int)getValue(), col);
    }
}
