package argssearch.shared.lemmatization;

import com.google.gson.JsonParser;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class to make use of the CoreNLP functions.
 */
public class CoreNlpService {

    private final StanfordCoreNLP pipeline;
    private final HashSet<String> stopWords;
    private static final Pattern pattern = Pattern.compile("[a-zA-Z]{2,}");

    public CoreNlpService() {
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        pipeline = new StanfordCoreNLP(properties);
        stopWords = new HashSet<>();
        initStopWords();
    }

    /**
     * Reads stop words from a list and put them into a HashSet.
     */
    private void initStopWords() {
        String path = getClass().getResource("/stopwords.json").getPath();
        try (Reader reader = new FileReader(path)) {
            JsonParser.parseReader(reader)
                    .getAsJsonObject()
                    .getAsJsonArray("words")
                    .iterator()
                    .forEachRemaining(jsonElement -> stopWords.add(jsonElement.getAsString()));
        } catch (IOException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * Lemmatizes a given document. This could be a single sentence or a real document as String.
     *
     * @param document document as String
     * @return A List of lemmatized tokens.
     */
    public List<String> lemmatize(final String document) {
        CoreDocument doc = new CoreDocument(document);
        pipeline.annotate(doc);

        return doc.tokens().stream()
                .map(CoreLabel::lemma)
                .filter(this::checkAgainstStopWords)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
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

    /**
     * Test our lemmatize function with an example.
     */
    private void test() {
        try {
            String file = Files.readString(Paths.get(getClass().getResource("/test/document.txt").getPath()));
            System.out.println(lemmatize(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CoreNlpService service = new CoreNlpService();
        service.test();
    }
}
