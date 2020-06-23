package argssearch.retrieval.models.vectorspace;

import argssearch.indexing.index.Weighter;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;

import java.util.List;

/**
 * Manages Vector Space retrieval.
 */
public class VectorSpace {

    private final DocumentTermMatrix matrix;
    private final Weighter weighter;
    private final CoreNlpService nlpService;

    /**
     * On construction a document term matrix will be created and filled with the document's terms weight.
     *
     * @param nlpService
     */
    public VectorSpace(CoreNlpService nlpService) {
        this.nlpService = nlpService;
        weighter = new Weighter();
        matrix = new DocumentTermMatrix();
        matrix.addDocuments(weighter);
    }

    /**
     * Execute query and retrieve results saved in a {@link Document}.
     *
     * @param query Search term.
     */
    public void query(String query) {
        Vector q = queryToVector(query);
        List<Document> results = matrix.processQuery(q);

        if (!results.isEmpty()) {
            for (int i = 0; i < 10; i++) {
                System.out.println(results.get(i));
            }
        }
    }

    /**
     * The query will be transformed to a Vector which saves the weight of the tokens of the query.
     *
     * @param query user query
     * @return Vector representation of query.
     */
    private Vector queryToVector(String query) {
        Vector vector = new Vector(weighter.getArgumentCount(), 0);
        List<String> tokens = nlpService.lemmatize(query);


        for (String token : tokens) {
            int id = ArgDB.getInstance().getIndexOfTerm(token) - 1;
            if (id >= 0) {
                vector.set(id, 1);
            }
        }

        return vector;
    }
}
