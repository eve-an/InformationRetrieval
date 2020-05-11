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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CoreNlpService {

    private final Properties properties;
    private final StanfordCoreNLP pipeline;
    private final HashSet<String> stopWords;
    private static final Pattern pattern = Pattern.compile("[a-zA-Z]{2,}");

    public CoreNlpService() {
        properties = new Properties();
        properties.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        pipeline = new StanfordCoreNLP(properties);
        stopWords = new HashSet<>();
        initStopWords();
    }

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

    public List<String> lemmatize(final String document) {
        CoreDocument doc = new CoreDocument(document);
        pipeline.annotate(doc);

        return doc.tokens().stream()
                .map(CoreLabel::lemma)
                .filter(this::checkAgainstStopWords)
                .collect(Collectors.toList());
    }

    private boolean checkAgainstStopWords(final String token) {
        return !stopWords.contains(token) && pattern.matcher(token).matches();
    }

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
