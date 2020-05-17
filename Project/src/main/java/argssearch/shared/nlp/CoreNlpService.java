package argssearch.shared.nlp;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
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
        // In case you dont need named entity recognition remove the ner property
        // It is the most expensive operation
        properties.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        pipeline = new StanfordCoreNLP(properties);
        stopWords = new HashSet<>();
        initStopWords();
    }

    /**
     * Reads stop words from a list and put them into a HashSet.
     */
    private void initStopWords() {
        String path = getClass().getResource("/stopwords.txt").getPath();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
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
        CoreDocument doc = new CoreDocument(document);
        pipeline.annotate(doc);

        return doc.tokens().stream()
                .map(CoreLabel::lemma)
                .filter(this::checkAgainstStopWords)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    /**
     * Gets the named entity relations.
     *
     * @param document document as String
     * @return A Map with tokens as keys and their ner as values.
     */
    public Map<String, String> namedEntities(final String document) {
        CoreDocument doc = new CoreDocument(document);
        pipeline.annotate(doc);

        Map<String, String> namedEntities = new HashMap<>();
        for (CoreEntityMention mention : doc.entityMentions()) {
            String entity = mention.text();
            String type = mention.entityType();

            if (checkAgainstStopWords(type)) {
                namedEntities.put(entity, type);
            }
        }


        return namedEntities;
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
