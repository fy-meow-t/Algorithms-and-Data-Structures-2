package comp3506.assn2.application;

import comp3506.assn2.utils.*;

/**
 * A standard trie structure storing texts and their positions in the searched document.
 *
 * Memory usage: O(n) where n is the total size of strings in the file
 *               In the worst case, every word in the file is distinct
 *               so the trie needs to store each word's all characters and occurrences
 */
public class TextTrie {

    // The root node of the trie
    private Internal root;
    // A special character representing the end of a word
    private static final char END = '\0';
    // Regx to ignore punctuations except apostrophes within a word
    private static final String FILTER = "[\\W&&[^']]|(?<![a-z])'|'(?![a-z])";

    /* --- Select different logic in searches --- */
    static final int AND = 1;
    static final int OR = 2;
    static final int NOT = 3;
    static final int COMPOUND = 4;

    /**
     * Constructor. Create an empty trie
     */
    TextTrie() {
        root = new Internal(END, null);
    }

    /* -------------------------- Methods related to insertion --------------------- */

    /**
     * Insert a string of words with their positions
     *
     * Run-time: O(dn) where d is size of the alphabet, n is total size of the string
     *
     *           When storing a text file, n is total size of the file since insertLine is called for each line
     *           The method calls "insertWithPos" for each word in the string
     *           "insertWithPos" calls "insertOneWord" which loops through each character of the word
     *           For each character, "contains" goes through all of its next characters to find a match
     *           which will be in the size of the alphabet in the worst case
     *
     * @param line The string containing one or more words
     * @param lineNo The line number
     * @param previous The leaf node of the previous word
     * @return The leaf node for the current occurrence of the inserted word
     */
    public Leaf insertLine(String line, int lineNo, Leaf previous) {
        int colNo = 1;
        String[] text = line.split(FILTER, -1);
        for (String word : text) {
            if (!word.equals("")) {
                previous = insertWithPos(word, lineNo, colNo, previous);
            }
            colNo += word.length() + 1;
        }
        return previous;
    }

    /**
     * Need to store the whole title string rather than store each word
     * since the section search must exactly match the stored title (e.g. cannot have extra whitespaces)
     *
     * Still use the Leaf as the end node of the title string. Set "col" to store the last line of the section.
     *
     * Run-time: O(dn) where d is size of the alphabet and n is total size of the string
     *
     *           When storing an index file, "insertIndex" is called for each line
     *           so the whole insertion takes O(dn) where d is size of the alphabet and n is total size of the file.
     *           Similar to "insertLine", "insertIndex" calls "insertWithPos", "insertOneWord", and "contains"
     *           to loop through each character of each word (takes n) and each next characters (takes d)
     *
     * @param indexLine a line in the index file
     */
    public Leaf insertIndex(String indexLine, Leaf lastSection, int totalLines) {
        int lastComma = indexLine.lastIndexOf(",");
        int lineNo = Integer.parseInt(indexLine.substring(lastComma + 1).trim());
        String title = indexLine.substring(0, lastComma);
        Leaf currentSection = insertWithPos(title, lineNo, totalLines, null);
        if (lastSection != null) {
            lastSection.setCol(lineNo - 1);
        }
        return currentSection;
    }

    /**
     * Insert one word with leaf nodes for positions
     *
     * @param word Word to insert into the trie
     * @param line The line number
     * @param col The column number of the first character
     * @param previous The leaf node of the previous word
     * @return The leaf node of the current word
     */
    private Leaf insertWithPos(String word, int line, int col, Leaf previous) {
        Pair<Internal, Internal> inserted = insertOneWord(word);
        Leaf index = new Leaf(line, col);
        index.setLastChar(inserted.getLeftValue());
        if (previous != null) {
            previous.setNextWord(index);
        }
        inserted.getRightValue().getChildren().addNode(index);
        return index;
    }

    /**
     * Insert one word without leaf nodes for positions
     *
     * @param word The word to insert
     * @return A pair of two internal nodes.
     *         The left one represents the last character and the right one represents the end
     */
    private Pair<Internal, Internal> insertOneWord(String word) {
        Internal current = root;
        Internal next;
        for (int i = 0; i < word.length(); i++) {
            next = contains(word.charAt(i), current.getChildren());
            if (next == null) {
                next = new Internal(word.charAt(i), current);
                current.getChildren().addNode(next);
            }
            current = next;
        }
        next = contains(END, current.getChildren());
        if (next == null) {
            next = new Internal(END, current);
            current.getChildren().addNode(next);
        }
        return new Pair<>(current, next);
    }

    /**
     * Find the internal node than contains a specific character
     *
     * @param item The character to search for
     * @param nodeList The list of internal nodes
     * @return The internal node for the character
     *         Null if no node contains the character
     */
    private Internal contains(char item, LinkedList nodeList) {
        Internal node = (Internal)nodeList.getHead();
        while (node != null) {
            if (node.getChar() == item) {
                return  node;
            }
            node =(Internal)node.getNext();
        }
        return null;
    }

    /* ----------------------- Methods related to basic searches ---------------- */

    /**
     * Get all occurrences of one word
     *
     * Run-time: O(dm) where d is size of the alphabet and m is size of the word to search for
     *
     *           Call "search" which goes through each character --> m
     *           For each character, "search" calls "contains" which loops through all children i.e. next characters
     *           which have size of the alphabet in the worst case --> d
     *
     * @param word the word to search for
     * @return A linked list of leaf node for occurrences
     *         The list is empty if the word is not found
     */
    public LinkedList getPos(String word) {
        Internal current = search(word);
        if (current != null) {
            Internal end = contains(END, current.getChildren());
            if (end != null) {
                return end.getChildren();
            }
        }
        return new LinkedList();
    }

    /**
     * Find all occurrence of a prefix
     *
     * Run-time: O(n) where n is total size of the file
     *
     *           In the worst case, the searched prefix is one character
     *           all words in the file are distinct and have the same prefix
     *           so the method reaches every node in the trie.
     *
     *           In a more common case, it takes O(dm) to reach the end of the prefix in the trie
     *           where d is size of the alphabet and m is size of the prefix
     *           Then getting all leaf nodes of the subtree takes O(s)
     *           where s is total size of words that have the prefix
     *           so the run-time is O(dm+s)
     *
     *           In the context, the file will be large,
     *           the size of the alphabet is 27 (English characters and apostrophe) which is relatively small
     *           and size of the prefix is usually small
     *           so the first case might be worse
     *
     * @param prefix the prefix to search for
     * @return A linked list of leaf nodes which represent occurrences
     *         The list is empty if the prefix is not found
     */
    public LinkedList searchPrefix (String prefix) {
        LinkedList indices = new LinkedList();
        Internal prefixEnd = search(prefix.toLowerCase());
        if (prefixEnd != null) {
            indices = getAllLeaves(prefixEnd);
        }
        return indices;
    }

    /**
     * Find all occurrences of a phrase
     *
     * Run-time: O(df + kr) where d is size of size of the alphabet,
     *          f is size of the first word of the phrase,
     *          k is the number of occurrences of the first word,
     *          r is size of the rest of the phrase
     *
     *          In the worst case, it takes O(df) to search the file for the first word of the phrase
     *          For each occurrence, "checkReverseWord" goes through each character of the rest of the phrase
     *
     * @param phrase The phrase to search for
     * @return A linked list of the occurrences.
     *         The list is empty if the phrase is not found
     */
    public LinkedList phraseIndices(String phrase) {
        int i = 0;
        String[] allWords = phrase.toLowerCase().split(" ");
        LinkedList occurrence = getPos(allWords[i]);
        LinkedList result = new LinkedList();

        Leaf pos = (Leaf) occurrence.getHead();
        while (pos != null) {
            i = 1;
            Leaf nextWord = pos.getNextWord();
            while (nextWord != null && i < allWords.length) {
                if (!checkReverseWord(allWords[i], nextWord)) {
                    break;
                }
                i++;
                nextWord = nextWord.getNextWord();
            }
            if (i == allWords.length) {
                result.addNode(new Leaf(pos.getLine(), pos.getCol()));
            }
            pos = (Leaf) pos.getNext();
        }
        return result;
    }

    /**
     * Search for a word or a prefix.
     * Find the internal node representing the last character of the searched term
     *
     * @param word The term to search for
     * @return The internal node for the last character
     *         Null if the term is not found
     */
    private Internal search(String word) {
        Internal current = root;
        Internal next;
        for (int i = 0; i < word.length(); i++) {
            next = contains(word.charAt(i), current.getChildren());
            if (next == null) {
                return null;
            }
            current = next;
        }
        return current;
    }

    /**
     * Recursive function to get all leaf nodes of the sub tree starting from an internal node
     *
     * @param current The root of the sub tree i.e. the last character of the prefix
     * @return A linked list of all leaf nodes i.e. Occurrences of all words having the prefix
     *         The list is empty if no leaf node is found
     */
    private LinkedList getAllLeaves(Internal current) {
        LinkedList indices  = new LinkedList();
        if (current.getChar() == END) {
            indices.addAll(current.getChildren());
        } else {
            Internal nextChar = (Internal) current.getChildren().getHead();
            while (nextChar != null) {
                indices.addAll(getAllLeaves(nextChar));
                nextChar = (Internal)nextChar.getNext();
            }
        }
        return indices;
    }

    /**
     * Check whether the word starting from the current position matches the searched term
     *
     * @param word The word to search for
     * @param end The leaf node representing the current position
     * @return True if the two words match. Otherwise false
     */
    private boolean checkReverseWord(String word, Leaf end) {
        int i = word.length() - 1;
        Internal preChar = end.getLastChar();
        while (i >= 0 && preChar.getChar() != END) {
            if (!(preChar.getChar() == word.charAt(i))) {
                return false;
            }
            i--;
            preChar = preChar.getPreChar();
        }
        return (preChar.getChar() == END && i == -1);
    }

    /* ----------------------- Methods related to logic searches ----------------- */

    /**
     * Search for lines that contain all (in AND mode) or at least one (in OR mode) of required words
     *
     * Run-time: O(dm + k) where d is size of the alphabet,
     *           m is total size of all words to search for,
     *           and k is sum of the occurrences of all searched words.
     *
     *           The method searches each word in both the stop-word trie and the document trie --> O(dn)
     *           Every word's all occurrences may need to be checked to get intersection or union. --> O(k)
     *
     * @param allWords Array of words to search for
     * @param mode AND or OR mode
     * @param stopWords Trie storing words to ignore in logic searches
     * @return A linked list of line numbers that meet the requirement
     *         The list is empty if no such line is found
     */
    public LinkedList andOrLine(String[] allWords, int mode, TextTrie stopWords) {
        LinkedList occurrence = new LinkedList();
        LinkedList index;
        for (String word : allWords) {
            if (stopWords != null && stopWords.getPos(word.toLowerCase()).getSize() != 0) {
                // The word is a stop-word
                continue;
            }
            index = getPos(word.toLowerCase());
            if (mode == AND && index.getSize() == 0) {
                return new LinkedList();
            }
            if (occurrence.getSize() != 0) {
                /* In AND mode: get the intersection of all searched words' line numbers
                                so that the result is list of lines that contain all of the words

                   In OR mode: get the union of all searched words's line numbers
                                so that the result is list of lines that contain at least one of the words
                 */
                occurrence = (mode == AND) ? intersect(occurrence, index) : union(occurrence, index);
            } else {
                // The occurrences of the first word to search for
                occurrence.addAll(index);
            }
        }
        return occurrence;
    }

    /**
     * Search for lines that contain all required words and none of excluded words
     *
     * Run-time: O(dm + k) where d is size of the alphabet,
     *          m is total size of all words to search for (including required words and excluded words),
     *          and k is sum of occurrences of all searched words
     *
     *          Calls "andOrLine" for both required words and excluded words
     *          "subtract" may goes through every occurrence
     *
     * @param wordsRequired Array of the required words
     * @param wordsExcluded Array of the excluded words
     * @param stopWords Trie storing the stop-words to ignore
     * @return Linked list of valid line numbers
     *         The list is empty if no such line is found
     */
    public LinkedList andNotLine(String[] wordsRequired, String[] wordsExcluded, TextTrie stopWords) {
        LinkedList required = andOrLine(wordsRequired, AND, stopWords);
        if (required.getSize() != 0) {
            // There exist lines that contain all the required words
            LinkedList excluded = andOrLine(wordsExcluded, OR, stopWords);
            return subtract(required, excluded);
        }
        return new LinkedList();
    }

    /**
     * Search the document for sections in different ways according to the mode:
     *      AND: Search for sections containing all the required words
     *      OR: Search for sections containing at least one of the required words
     *      NOT: Search for sections containing all the required words
     *           and none of the words in the "otherWords" parameter
     *      COMPOUND: Search for sections containing all the required words
     *                and at least one of the words in the "otherWords" parameter
     *
     * Run-time: O(dt + s(dm + k)) where d is size of the alphabet,
     *           t is total size of the titles,
     *           s is the number of sections,
     *           m is total size of the searched words (including required and "other words")
     *
     *           For each section, its title is searched in the index trie --> O(dt) for all sections
     *           "logicSearches" calls "SectionLogic"
     *           "SectionLogic" searches both stop-word trie and the doc trie for each word
     *           then goes through all occurrences to check their line numbers
     *
     * @param allTitles Array of titles of the sections to search within
     *                  The entire document is searched if allTitles is null or an empty array.
     * @param wordsRequired Array of required words
     * @param otherWords Array of "or words" or excluded words
     * @param mode The specific logic: AND, OR, NOT, or COMPOUND
     * @param stopWords Trie storing the stop-words to ignore
     * @param indexTrie Trie storing the titles and their starting and ending line numbers
     * @param allLine The total line number of the document
     * @return Linked list of triples for each occurrence of words found in all sections
     *         Left value: the line number. Center value: the column number. Right value: word found
     *         The list is empty if no word found or all indicated titles are not part of the document
     */
    public LinkedList searchForSections(String[] allTitles, String[] wordsRequired, String[] otherWords, int mode,
                                        TextTrie stopWords, TextTrie indexTrie, int allLine) {
        LinkedList result = new LinkedList();
        LinkedList newList;
        if (allTitles == null || allTitles.length == 0) {
            // Search the entire document
            return logicSearches(wordsRequired, otherWords, stopWords, 1, allLine, mode);
        }

        int startLine, endLine;
        LinkedList titleOccurrence;
        Leaf end;
        for (String title : allTitles) {
            titleOccurrence = indexTrie.getPos(title);
            end = (Leaf) titleOccurrence.getHead();
            // The document may have sections that have the same title
            while (end != null) {
                startLine = end.getLine();
                endLine = end.getCol();
                newList = logicSearches(wordsRequired, otherWords, stopWords, startLine, endLine, mode);
                if (newList != null) {
                    /* newList is created in logicSearches not the list of leaf nodes in the trie
                       so it can be directly added to the result
                     */
                    result.addNode(newList.getHead());
                    result.setTail(newList.getTail());
                }
                end = (Leaf) end.getNext();
            }
        }
        return result;
    }

    /**
     * Direct the searches within one section
     * Call the sectionLogic method in different ways according to the logic mode
     *
     * @param wordsRequired The required words to search for
     * @param otherWords The "or words" or excluded words
     * @param stopWords The trie storing stop-words to ignore
     * @param startLine The first line of the section
     * @param endLine The last line of the section
     * @param mode The specific logic: AND, OR, NOT, or COMPOUND
     * @return Searching result returned by the sectionLogic method.
     *         A linked list of triples for each valid occurrence of searched word in the section
     *         Left value: the line number. Center value: the column number. Right value: word found
     *         Null if the section does not meet the searching criteria
     */
    private LinkedList logicSearches (String[] wordsRequired, String[]otherWords, TextTrie stopWords,
                                      int startLine, int endLine, int mode) {
        if (mode == AND || mode == OR) {
            return sectionLogic(wordsRequired, startLine, endLine, stopWords, mode);
        }
        if (mode == NOT && sectionLogic(otherWords, startLine, endLine, stopWords, NOT) != null) {
            return sectionLogic(wordsRequired, startLine, endLine, stopWords, AND);
        }
        if (mode == COMPOUND) {
            LinkedList andList = sectionLogic(wordsRequired, startLine, endLine, stopWords, AND);
            LinkedList orList = sectionLogic(otherWords, startLine, endLine, stopWords, OR);
            if (andList != null && orList != null) {
                andList.addNode(orList.getHead());
                andList.setTail(orList.getTail());
                return andList;
            }
        }
        return null;
    }

    /**
     * Conduct the logic searches within one section
     *
     * @param allWords List of words to search for
     * @param startLine The first line of the section
     * @param endLine The last line of the section
     * @param stopWords The trie storing stop-words to ignore
     * @param mode The specific logic: AND, OR, or NOT
     * @return A linked list of triples for each valid occurrence of searched word in the section
     *         Left value: the line number. Center value: the column number. Right value: word found
     *         Null if (OR mode) all words are not found
     *                 (AND mode) at least one of the words is not found
     *                 (NOT mode) at least one of the words is found
     */
    private LinkedList sectionLogic(String[] allWords, int startLine, int endLine, TextTrie stopWords, int mode) {
        LinkedList result = new LinkedList();
        LinkedList index;
        Leaf end;
        boolean hasThisWord;

        if ((mode == NOT || mode == OR) && (allWords == null || allWords.length == 0)) {
            // the excludedWords in not search or the orWords in compound search is empty or null
            return result;
        }

        for (String word : allWords) {
            word = word.toLowerCase();
            if (stopWords != null && stopWords.getPos(word).getSize() != 0) {
                // The word is a stop-word
                continue;
            }

            hasThisWord = false;
            index = getPos(word);
            end = (Leaf) index.getHead();
            while (end != null) {
                if (end.getLine() >= startLine && end.getLine() <= endLine) {
                    if (mode == NOT) {
                        return null;
                    }
                    result.addNode(new Node(new Triple<>(end.getLine(), end.getCol(), word)));
                    hasThisWord = true;
                } else if (end.getLine() > endLine) {
                    break;
                }
                end = (Leaf) end.getNext();
            }
            if (mode == AND && !hasThisWord) {
                return null;
            }
        }
        if (mode == OR && result.getSize() == 0) {
            return null;
        }
        return result;
    }

    /*
     * Intersect, union, subtract: both lists as parameters are sorted increasingly
     *
     * The reader reads the file and the trie inserts nodes from the first word to the last
     * so that in the occurrence list (singly linked list of leaf nodes)
     * the previous node's line number is smaller than the next one's
     * or they have the same line numbers but the previous column number is smaller.
     */

    /**
     * Get the intersection of two lists
     *
     * @param lineSet The current set of line numbers which contain all of words that have been searched
     *                or the first searched word's occurrences (leaf nodes)
     * @param nextWord Occurrences of the next searched word. A linked list of its leaf nodes
     * @return Intersection set of two lists: lines that contain all of previously and currently searched words
     */
    private LinkedList intersect(LinkedList lineSet, LinkedList nextWord) {
        LinkedList result = new LinkedList();
        Node first = lineSet.getHead();
        Leaf second = (Leaf) nextWord.getHead();
        int value, line;
        while (first != null && second != null) {
            value = (int)first.getValue();
            line = second.getLine();
            if ( value < line) {
                first = first.getNext();
            } else if(value > line) {
                second = (Leaf)second.getNext();
            } else {
                if (result.getTail() == null || (int)result.getTail().getValue() != line) {
                    /* The list of occurrences may contain duplicate line numbers
                       which means the word appears more than once in the line.
                       To make the result a set, only add values that are not in the result
                    */
                    result.addNode(new Node(line));
                }
                first = first.getNext();
                second = (Leaf)second.getNext();
            }
        }
        return result;
    }

    /**
     * Get the union of two lists
     *
     * @param lineSet The current set of lines numbers which contain at least on of words that have been searched
     *                or the first searched word's occurrences (leaf nodes)
     * @param nextWord Occurrences of the next searched word. A linked list of its leaf nodes
     * @return Union set of two lists: lines that contain at least one of previously and currently searched words
     */
    private LinkedList union (LinkedList lineSet, LinkedList nextWord) {
        LinkedList result = new LinkedList();
        Node first = lineSet.getHead();
        Leaf second = (Leaf) nextWord.getHead();
        int value, line;
        while (first != null && second != null) {
            value = (int)first.getValue();
            line = second.getLine();
            if (value < line) {
                if (result.getTail() == null || (int)result.getTail().getValue() != value) {
                    result.addNode(new Node(value));
                }
                first = first.getNext();
            } else if(value > line) {
                if (result.getTail() == null || (int)result.getTail().getValue() != line) {
                    result.addNode(new Node(line));
                }
                second = (Leaf)second.getNext();
            } else {
                if (result.getTail() == null || (int)result.getTail().getValue() != value) {
                    result.addNode(new Node(value));
                }
                first = first.getNext();
                second = (Leaf)second.getNext();
            }
        }
        while (first != null) {
            value = (int)first.getValue();
            if (result.getTail() == null || (int)result.getTail().getValue() != value) {
                result.addNode(new Node(value));
            }
            first = first.getNext();
        }
        while (second != null) {
            line = second.getLine();
            if (result.getTail() == null || (int)result.getTail().getValue() != line) {
                result.addNode(new Node(line));
            }
            second = (Leaf)second.getNext();
        }
        return result;
    }

    /**
     * Get the set of lines that are in the required list but not in the excluded list
     * The two lists are created in the andOrLine method, not the original list of leaf nodes (occurrences) in the trie
     * so they can be manipulated directly without creating new copies
     *
     * @param requiredList A linked list (set) of line numbers that contain all required words
     * @param excludeList A linked list (set) of line numbers that contain at least one excluded word
     * @return Set of lines that contain all required words but none of excluded words
     */
    private LinkedList subtract(LinkedList requiredList, LinkedList excludeList) {
        LinkedList result = new LinkedList();
        Node required = requiredList.getHead();
        Node exclude = excludeList.getHead();
        int requiredLine, excludeLine;

        while (required != null && exclude != null) {
            requiredLine = (int) required.getValue();
            excludeLine = (int) exclude.getValue();
            if (requiredLine < excludeLine) {
                result.addNode(required);
                required = required.getNext();
            } else if (requiredLine > excludeLine) {
                exclude = exclude.getNext();
            } else {
                // The line includes all required words and at least one excluded word
                required = required.getNext();
                exclude = exclude.getNext();
            }
        }

        if (required != null) {
            // More lines contain all required words while no line contains excluded words
            result.addNode(required);
            // The rest of nodes in the required list are linked by the current node "required"
            result.setTail(requiredList.getTail());
        }

        return result;
    }
}
