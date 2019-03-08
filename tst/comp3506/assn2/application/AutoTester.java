package comp3506.assn2.application;

import comp3506.assn2.utils.*;
import static comp3506.assn2.application.TextTrie.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hook class used by automated testing tool.
 * The testing tool will instantiate an object of this class to test the functionality.
 *
 * Assumption: the searching input will not include punctuation, aside from apostrophes inside a word.
 * 			   the index file and stop words file are correctly formatted.
 * 
 * @author Feiyue Tao s4473404
 */
public class AutoTester implements Search {

    private TextTrie docTrie;			// The trie storing the searched document texts
	private int lineNo;					// The total line number of the document
    private TextTrie indexTrie;			// The trie storing the index (titles and line numbers)
    private TextTrie stopWordsTrie;		// The trie storing the stop-words to ignore in logic searches

	/**
	 * Create an object that performs search operations on a document.
	 * If indexFileName or stopWordsFileName are null or an empty string the document should be loaded
	 * and all searches will be across the entire document with no stop words.
	 * All files are expected to be in the files sub-directory and 
	 * file names are to include the relative path to the files (e.g. "files\\shakespeare.txt").
	 * 
	 * @param documentFileName  Name of the file containing the text of the document to be searched.
	 * @param indexFileName     Name of the file containing the index of sections in the document.
	 * @param stopWordsFileName Name of the file containing the stop words ignored by most searches.
	 * @throws FileNotFoundException if any of the files cannot be loaded. 
	 *                               The name of the file(s) that could not be loaded should be passed 
	 *                               to the FileNotFoundException's constructor.
	 * @throws IllegalArgumentException if documentFileName is null or an empty string.
	 */
	public AutoTester(String documentFileName, String indexFileName, String stopWordsFileName) 
			throws FileNotFoundException, IllegalArgumentException {
        checkString(documentFileName);
        docTrie = new TextTrie();
        indexTrie = new TextTrie();
        stopWordsTrie = new TextTrie();
		String line;
		BufferedReader reader;

		// Build the document trie
		try {
			reader = new BufferedReader(new FileReader(documentFileName));
			Leaf previous = null;
			while ((line = reader.readLine()) != null) {
				lineNo++;
				previous = docTrie.insertLine(line.toLowerCase(), lineNo, previous);
			}
			reader.close();
		} catch (IOException e) {
			throw new FileNotFoundException(documentFileName);
		}

		if (indexFileName != null && !indexFileName.equals("")) {
			// Build the index trie
			try {
				reader = new BufferedReader(new FileReader(indexFileName));
				Leaf lastSection = null;
				while ((line = reader.readLine()) != null) {
				    if(!line.equals("")) {
                        lastSection = indexTrie.insertIndex(line, lastSection, lineNo);
                    }
				}
				reader.close();
			} catch (IOException e) {
				throw new FileNotFoundException(indexFileName);
			}
		}

		if (stopWordsFileName != null && !stopWordsFileName.equals("")) {
			// Build the stop-words trie
			try {
			reader = new BufferedReader(new FileReader(stopWordsFileName));
			while ((line = reader.readLine()) != null) {
				stopWordsTrie.insertLine(line.toLowerCase(), 1, null);
			}
			reader.close();
			} catch (IOException e) {
				throw new FileNotFoundException(stopWordsFileName);
			}
		}
	}

	/**
	 * Determines the number of times the word appears in the document.
	 *
	 * @param word The word to be counted in the document.
	 * @return The number of occurrences of the word in the document.
	 * @throws IllegalArgumentException if word is null or an empty String.
	 */
	@Override
	public int wordCount(String word) throws IllegalArgumentException {
		checkString(word);
		LinkedList result = docTrie.getPos(word.toLowerCase());
		return result.getSize();
	}

	/**
	 * Finds all occurrences of the phrase in the document.
	 * A phrase may be a single word or a sequence of words.
	 *
	 * @param phrase The phrase to be found in the document.
	 * @return List of pairs, where each pair indicates the line and column number of each occurrence of the phrase.
	 *         Returns an empty list if the phrase is not found in the document.
	 * @throws IllegalArgumentException if phrase is null or an empty String.
	 */
	@Override
	public List<Pair<Integer, Integer>> phraseOccurrence(String phrase) throws IllegalArgumentException {
	    checkString(phrase);
		return getPosList(docTrie.phraseIndices(phrase));
	}

	/**
	 * Finds all occurrences of the prefix in the document.
	 * A prefix is the start of a word. It can also be the complete word.
	 * For example, "obscure" would be a prefix for "obscure", "obscured", "obscures" and "obscurely".
	 *
	 * @param prefix The prefix of a word that is to be found in the document.
	 * @return List of pairs, where each pair indicates the line and column number of each occurrence of the prefix.
	 *         Returns an empty list if the prefix is not found in the document.
	 * @throws IllegalArgumentException if prefix is null or an empty String.
	 */
	@Override
	public List<Pair<Integer, Integer>> prefixOccurrence(String prefix) throws IllegalArgumentException {
	    checkString(prefix);
		return (getPosList(docTrie.searchPrefix(prefix)));
	}

	/**
	 * Searches the document for lines that contain all the words in the 'words' parameter.
	 * Implements simple "and" logic when searching for the words.
	 * The words do not need to be contiguous on the line.
	 *
	 * @param words Array of words to find on a single line in the document.
	 * @return List of line numbers on which all the words appear in the document.
	 *         Returns an empty list if the words do not appear in any line in the document.
	 * @throws IllegalArgumentException if words is null or an empty array
	 *                                  or any of the Strings in the array are null or empty.
	 */
	@Override
	public List<Integer> wordsOnLine(String[] words) throws IllegalArgumentException {
		checkArgArray(words, true);
		return getLineList(docTrie.andOrLine(words, AND, stopWordsTrie));
	}

	/**
	 * Searches the document for lines that contain any of the words in the 'words' parameter.
	 * Implements simple "or" logic when searching for the words.
	 * The words do not need to be contiguous on the line.
	 *
	 * @param words Array of words to find on a single line in the document.
	 * @return List of line numbers on which any of the words appear in the document.
	 *         Returns an empty list if none of the words appear in any line in the document.
	 * @throws IllegalArgumentException if words is null or an empty array
	 *                                  or any of the Strings in the array are null or empty.
	 */
	@Override
	public List<Integer> someWordsOnLine(String[] words) throws IllegalArgumentException {
		checkArgArray(words, true);
		return getLineList(docTrie.andOrLine(words, OR, stopWordsTrie));
	}

	/**
	 * Searches the document for lines that contain all the words in the 'wordsRequired' parameter
	 * and none of the words in the 'wordsExcluded' parameter.
	 * Implements simple "not" logic when searching for the words.
	 * The words do not need to be contiguous on the line.
	 *
	 * @param wordsRequired Array of words to find on a single line in the document.
	 * @param wordsExcluded Array of words that must not be on the same line as 'wordsRequired'.
	 * @return List of line numbers on which all the wordsRequired appear
	 *         and none of the wordsExcluded appear in the document.
	 *         Returns an empty list if no lines meet the search criteria.
	 * @throws IllegalArgumentException if either of wordsRequired or wordsExcluded are null or an empty array
	 *                                  or any of the Strings in either of the arrays are null or empty.
	 */
	@Override
	public List<Integer> wordsNotOnLine(String[] wordsRequired, String[] wordsExcluded) throws IllegalArgumentException {
		checkArgArray(wordsRequired, true);
		checkArgArray(wordsExcluded, true);
		return getLineList(docTrie.andNotLine(wordsRequired, wordsExcluded, stopWordsTrie));
	}

	/**
	 * Searches the document for sections that contain all the words in the 'words' parameter.
	 * Implements simple "and" logic when searching for the words.
	 * The words do not need to be on the same lines.
	 *
	 * @param titles Array of titles of the sections to search within,
	 *               the entire document is searched if titles is null or an empty array.
	 * @param words Array of words to find within a defined section in the document.
	 * @return List of triples, where each triple indicates the line and column number and word found,
	 *         for each occurrence of one of the words.
	 *         Returns an empty list if the words are not found in the indicated sections of the document,
	 *         or all the indicated sections are not part of the document.
	 * @throws IllegalArgumentException if words is null or an empty array
	 *                                  or any of the Strings in either of the arrays are null or empty.
	 */
	@Override
	public List<Triple<Integer, Integer, String>> simpleAndSearch(String[] titles, String[] words) throws IllegalArgumentException {
		checkArgArray(words, true);
		checkArgArray(titles, false);
		return getTripleList(docTrie.searchForSections(titles, words, null, AND, stopWordsTrie, indexTrie, lineNo));
	}

	/**
	 * Searches the document for sections that contain any of the words in the 'words' parameter.
	 * Implements simple "or" logic when searching for the words.
	 * The words do not need to be on the same lines.
	 *
	 * @param titles Array of titles of the sections to search within,
	 *               the entire document is searched if titles is null or an empty array.
	 * @param words Array of words to find within a defined section in the document.
	 * @return List of triples, where each triple indicates the line and column number and word found,
	 *         for each occurrence of one of the words.
	 *         Returns an empty list if the words are not found in the indicated sections of the document,
	 *         or all the indicated sections are not part of the document.
	 * @throws IllegalArgumentException if words is null or an empty array
	 *                                  or any of the Strings in either of the arrays are null or empty.
	 */
	@Override
	public List<Triple<Integer, Integer, String>> simpleOrSearch(String[] titles, String[] words) throws IllegalArgumentException {
		checkArgArray(words, true);
		checkArgArray(titles, false);
		return getTripleList(docTrie.searchForSections(titles, words, null,
                OR, stopWordsTrie, indexTrie, lineNo));
	}

	/**
	 * Searches the document for sections that contain all the words in the 'wordsRequired' parameter
	 * and none of the words in the 'wordsExcluded' parameter.
	 * Implements simple "not" logic when searching for the words.
	 * The words do not need to be on the same lines.
	 *
	 * @param titles Array of titles of the sections to search within,
	 *               the entire document is searched if titles is null or an empty array.
	 * @param wordsRequired Array of words to find within a defined section in the document.
	 * @param wordsExcluded Array of words that must not be in the same section as 'wordsRequired'.
	 * @return List of triples, where each triple indicates the line and column number and word found,
	 *         for each occurrence of one of the required words.
	 *         Returns an empty list if the words are not found in the indicated sections of the document,
	 *         or all the indicated sections are not part of the document.
	 * @throws IllegalArgumentException if wordsRequired is null or an empty array
	 *                                  or any of the Strings in any of the arrays are null or empty.
	 */
	@Override
	public List<Triple<Integer, Integer, String>> simpleNotSearch(String[] titles, String[] wordsRequired, String[] wordsExcluded) throws IllegalArgumentException {
        checkArgArray(wordsRequired, true);
        checkArgArray(titles, false);
        checkArgArray(wordsExcluded, false);
        return getTripleList(docTrie.searchForSections(titles, wordsRequired, wordsExcluded,
                NOT, stopWordsTrie, indexTrie, lineNo));
	}

	/**
	 * Searches the document for sections that contain all the words in the 'wordsRequired' parameter
	 * and at least one of the words in the 'orWords' parameter.
	 * Implements simple compound "and/or" logic when searching for the words.
	 * The words do not need to be on the same lines.
	 *
	 * @param titles Array of titles of the sections to search within,
	 *               the entire document is searched if titles is null or an empty array.
	 * @param wordsRequired Array of words to find within a defined section in the document.
	 * @param orWords Array of words, of which at least one, must be in the same section as 'wordsRequired'.
	 * @return List of triples, where each triple indicates the line and column number and word found,
	 *         for each occurrence of one of the words.
	 *         Returns an empty list if the words are not found in the indicated sections of the document,
	 *         or all the indicated sections are not part of the document.
	 * @throws IllegalArgumentException if wordsRequired is null or an empty array
	 *                                  or any of the Strings in any of the arrays are null or empty.
	 */
	@Override
	public List<Triple<Integer, Integer, String>> compoundAndOrSearch(String[] titles, String[] wordsRequired, String[] orWords) throws IllegalArgumentException {
        checkArgArray(wordsRequired, true);
        checkArgArray(titles, false);
        checkArgArray(orWords, false);
	    return getTripleList(docTrie.searchForSections(titles, wordsRequired, orWords,
                COMPOUND, stopWordsTrie, indexTrie, lineNo));
	}

	/**
	 * Check whether the string is valid
	 *
	 * @param arg the string to check
	 * @throws IllegalArgumentException if the string is null or an empty string
	 */
	private void checkString (String arg) throws IllegalArgumentException {
		if (arg == null || arg.equals("")) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Check whether the string Array is valid
	 *
	 * @param words the array to check
	 * @param mustExist true is the array cannot be null or an empty array
	 * @throws IllegalArgumentException if any string in the array is null or an empty string
	 * 									if mustExist is true and the array is null or an empty array
	 */
	private void checkArgArray(String[] words, boolean mustExist) throws IllegalArgumentException {
		if (words != null && words.length > 0) {
			for (String word : words) {
				checkString(word);
			}
		} else if (mustExist) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Turn the linked list of integer into an array list of integer
	 *
	 * @param occurrence the linked list of integer
	 * @return an array list of integer
	 */
	private List<Integer> getLineList(LinkedList occurrence) {
		List<Integer> result = new ArrayList<>();
		Node node = occurrence.getHead();
		while (node != null) {
			result.add((int)node.getValue());
			node = node.getNext();
		}
		return result;
	}

	/**
	 * Turn the linked list of Leaf into an array list of Pair
	 *
	 * @param occurrence the linked list of Leaf nodes
	 * @return an array list of Pair whose left value is the line number and right value is the column number
	 */
	private List<Pair<Integer, Integer>> getPosList (LinkedList occurrence) {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		Leaf node = (Leaf) occurrence.getHead();
		while (node != null) {
			result.add(new Pair<>(node.getLine(), node.getCol()));
			node = (Leaf) node.getNext();
		}
		return result;
	}

	/**
	 *
	 * Turn the linked list into an array list
	 *
	 * @param occurrence the linked list
	 * @return the array list
	 */
	@SuppressWarnings("unchecked")
	private List<Triple<Integer, Integer, String>> getTripleList(LinkedList occurrence) {
		List<Triple<Integer, Integer, String>> result = new ArrayList<>();
		Node node = occurrence.getHead();
		while (node != null) {
			result.add((Triple<Integer, Integer, String>) node.getValue());
			node = node.getNext();
		}
		return result;
	}
}
