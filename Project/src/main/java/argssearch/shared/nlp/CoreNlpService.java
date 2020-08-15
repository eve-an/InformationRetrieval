package argssearch.shared.nlp;

import edu.stanford.nlp.simple.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class to make use of the CoreNLP functions.
 */
public class CoreNlpService {

    private final HashSet<String> stopWords;
    private static final Pattern pattern = Pattern.compile("[a-zA-Z]{2,}");

    public CoreNlpService() throws URISyntaxException {
        // In case you dont need named entity recognition remove the ner property
        // It is the most expensive operation
        stopWords = new HashSet<>();
        initStopWords();
    }

    /**
     * Reads stop words from a list and put them into a HashSet.
     */
    private void initStopWords() throws URISyntaxException {
        URL resPath = getClass().getResource("/stopwords.txt");
        File stopwordsFile = Paths.get(resPath.toURI()).toFile();

        try (BufferedReader br = new BufferedReader(new FileReader(stopwordsFile))) {
            String word;
            while ((word = br.readLine()) != null) {
                if (!word.startsWith("//")) {    // Comments
                    stopWords.add(word);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lemmatizes a given document. This could be a single sentence or a real document as String.
     *
     * @param document document as String
     * @return A List of lemmatized tokens.
     */
    public List<String> lemmatize(final String document) {
        Document doc = new Document(document);

        return doc.sentences().stream()
                .flatMap(sentence -> sentence.lemmas().stream())
                .filter(this::checkAgainstStopWords)
                .map(String::toLowerCase).collect(Collectors.toList());

    }

    /**
     * Checks if a token is a stop word and matches the regex.
     * The regex accepts only words with alphabetical characters and a minimum length of 2.
     *
     * @param token token to check.
     * @return true if token matches our conditions.
     */
    private boolean checkAgainstStopWords(final String token) {
        return !stopWords.contains(token) && pattern.matcher(token).matches();
    }
}
