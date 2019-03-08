package comp3506.assn2.testdriver;


import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import comp3506.assn2.application.AutoTester;
import comp3506.assn2.application.Search;
import comp3506.assn2.utils.TestingTriple;
import comp3506.assn2.utils.TestingPair;
import comp3506.assn2.utils.Triple;
import comp3506.assn2.utils.Pair;


/**
 * Sample tests for each method in comp3506.assn2.application.Search
 * 
 * @author Richard Thomas <richard.thomas@uq.edu.au>
 *
 */
public class SimpleTests {

	private static Search searchApplication;
	
	// All occurrences of the word "obscure" in shakespeare.txt.
	private final static List<TestingPair<Integer,Integer>> obscureOccurrences =
			Arrays.asList(new TestingPair<>(5,5),
					new TestingPair<>(7,10),
					new TestingPair<>(9,25),
					new TestingPair<>(13,38));

	
	@BeforeClass
	public static void openFiles() {
		try {
			searchApplication = new AutoTester("files\\new.txt", "files\\new index.txt", "files\\stop-words.txt");
		} catch (FileNotFoundException | IllegalArgumentException e) {
			System.out.println("Opening files failed!");
			e.printStackTrace();
		}
	}

	@Test(timeout=500)
	public void testWordCount() {
		assertThat("Word count of 'of' should have been 6.", searchApplication.wordCount("train'd"), is(equalTo(4)));
	}

	@Test(timeout=500)
	public void testPhraseOccurrence_FirstOccurrence() {
		List<TestingPair<Integer, Integer>> searchResult = makeTestingPair(searchApplication.phraseOccurrence("for"));
		for (TestingPair pair : searchResult) {
			System.out.println(String.format("line: %d, col: %d", pair.getLeftValue(), pair.getRightValue()));
		}
	}

	@Test(timeout=500)
	public void testPhraseOccurrence_LastOccurrence() {
		List<TestingPair<Integer, Integer>> searchResult = makeTestingPair(searchApplication.phraseOccurrence("obscure"));
		assertThat("Last occurrence of 'obscure' was not found.", searchResult, hasItem(obscureOccurrences.get(obscureOccurrences.size()-1)));
	}

	@Test(timeout=500)
	public void testPhraseOccurrence_MiddleOccurrence() {
		List<TestingPair<Integer, Integer>> searchResult = makeTestingPair(searchApplication.phraseOccurrence("obscure"));
		assertThat("A middle occurrence of 'obscure' was not found.", searchResult, hasItem(obscureOccurrences.get(obscureOccurrences.size()/2)));
	}

	@Test(timeout=500)
	public void testPhraseOccurrence_AllOccurrences() {
		List<TestingPair<Integer, Integer>> searchResult = makeTestingPair(searchApplication.phraseOccurrence("obscure"));
		assertThat("Locations of 'obscure' were not expected.", searchResult, containsInAnyOrder(obscureOccurrences.toArray()));
		assertThat("Search for 'obscure' returned wrong number of results.", searchResult, hasSize(obscureOccurrences.size()));
	}

	@Test(timeout=500)
	public void testPhraseOccurrence_MultipleWords() {
		List<TestingPair<Integer, Integer>> searchResult = makeTestingPair(searchApplication.phraseOccurrence("that is the question"));
		assertThat("Phrase 'that is the question' was not found where expected.", searchResult, hasItem(new TestingPair<>(25779, 22)));
		assertThat("Search for 'that is the question' returned wrong number of results.", searchResult, hasSize(1));
	}

	@Test(timeout=500)
	public void testPrefixOccurrence() {
		List<TestingPair<Integer,Integer>> expected = new ArrayList<>(obscureOccurrences);
		List<TestingPair<Integer,Integer>> searchResult = makeTestingPair(searchApplication.prefixOccurrence("dE"));
		for (TestingPair pair : searchResult) {
			System.out.println(String.format("line: %d, col: %d", pair.getLeftValue(), pair.getRightValue()));
		}
	}

	@Test(timeout=500)
	public void testWordsOnLine() {
		String [] searchTerm = {"dfsfd","ObScuRe", "AND","of", "his","be"};
		List<Integer> expected = Arrays.asList();
		assertThat("Location of 'riper' && 'decease' were not expected.", searchApplication.wordsOnLine(searchTerm), is(expected));
	}

	@Test(timeout=500)
	public void testSomeWordsOnLine() {
		String [] searchTerm = {"sdasf","g", "be", "p", "AND"};
		List<Integer> expected = Arrays.asList();
		List<Integer> searchResult = searchApplication.someWordsOnLine(searchTerm);
		for (int num : searchResult) {
			System.out.println(num);
		}
	}

	@Test(timeout=500)
	public void testWordsNotOnLine() {
		String [] requiredWords = {"third", "of", "prince"};
		String [] excludedWords = {};
		List<Integer> expected = Arrays.asList(28);
		List<Integer> searchResult = searchApplication.wordsNotOnLine(requiredWords, excludedWords);
		assertThat("Locations of 'riper' were not expected.", searchResult, containsInAnyOrder(expected.toArray()));
		assertThat("Search for 'riper' && !'decease' returned wrong number of results.", searchResult, hasSize(expected.size()));
	}

	@Test(timeout=500)
	public void testSimpleAndSearch() {
		String [] titles = null;
		String [] requiredWords = {"be", "death", "his"};
		List<TestingTriple<Integer,Integer,String>> expected = Arrays.asList();


		List<TestingTriple<Integer,Integer,String>> searchResult = makeTestingTriple(searchApplication.simpleAndSearch(titles, requiredWords));

		for (TestingTriple triple : searchResult) {
			System.out.println(String.format("Line: %d, col: %d, word: %s", triple.getLeftValue(), triple.getCentreValue(), triple.getRightValue()));
		}

	}

	@Test(timeout=500)
	public void testSimpleOrSearch() {
		String [] titles = {"first"};
		String [] words = null;
		List<TestingTriple<Integer,Integer,String>> expected = Arrays.asList(new TestingTriple<>(18, 18,"skdffkj"),
				                                                             new TestingTriple<>(22, 1,"dskf")
				                                                             );

		List<Triple<Integer,Integer,String>> result = searchApplication.simpleOrSearch(titles, words);
		List<TestingTriple<Integer,Integer,String>> searchResult = makeTestingTriple(result);
		for (Triple triple : searchResult) {
			System.out.println(String.format("line: %d, col: %d, word: %s.", triple.getLeftValue(), triple.getCentreValue(), triple.getRightValue()));
		}

	}

	@Test(timeout=500)
	public void testSimpleNotSearch() {

		String [] titles = {"first", "second"};
		String [] requiredWords = {};
		String [] excludedWords = {};
		List<TestingTriple<Integer,Integer,String>> expected = Arrays.asList(new TestingTriple<>(100683,31,"rusty"),    // King Richard the Second
                                                                             new TestingTriple<>(100957,31,"obscure"));
		List<TestingTriple<Integer,Integer,String>> searchResult = 
				makeTestingTriple(searchApplication.simpleNotSearch(titles, requiredWords, excludedWords));
		for (Triple triple : searchResult) {
			System.out.println(String.format("line: %d, col: %d, word: %s.", triple.getLeftValue(), triple.getCentreValue(), triple.getRightValue()));
		}
	}

	@Test(timeout=500)
	public void testCompoundAndOrSearch() {
		String [] titles = null;
		String [] requiredWords = null;
		String [] orWords = null;
		List<TestingTriple<Integer,Integer,String>> expected = Arrays.asList(new TestingTriple<>(23709,29,"beaver"),    // Hamlet
                                                                             new TestingTriple<>(27960,25,"obscure"),
                                                                             new TestingTriple<>(148012,31,"obscure"),  // Venus and Adonis
                                                                             new TestingTriple<>(148047,33,"hoof"));
		List<TestingTriple<Integer,Integer,String>> searchResult = 
				makeTestingTriple(searchApplication.compoundAndOrSearch(titles, requiredWords, orWords));
		for (Triple triple : searchResult) {
			System.out.println(String.format("line: %d, col: %d, word: %s.", triple.getLeftValue(), triple.getCentreValue(), triple.getRightValue()));
		}
	}

	
	/**
	 * @param data The list of Pairs to be converted to a list of TestingPairs.
	 */
	private List<TestingPair<Integer, Integer>> makeTestingPair(List<Pair<Integer, Integer>> data) {
		List<TestingPair<Integer, Integer>> result = new ArrayList<>(); 
		for (Pair<Integer, Integer> pair: data) {
			result.add(new TestingPair<Integer, Integer>(pair));
		}
		return result;
	}

	/**
	 * @param data The list of Triples to be converted to a list of TestingTriples.
	 */
	private List<TestingTriple<Integer, Integer, String>> makeTestingTriple(List<Triple<Integer, Integer, String>> data) {
		List<TestingTriple<Integer, Integer, String>> result = new ArrayList<>(); 
		for (Triple<Integer, Integer, String> triple: data) {
			result.add(new TestingTriple<Integer, Integer, String>(triple));
		}
		return result;
	}

}
