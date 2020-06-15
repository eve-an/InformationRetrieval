package argssearch.retrieval.models.vectorspace;

import argssearch.indexing.index.Weighter;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;

import java.util.List;

public class VectorSpace {

    private final DocumentTerm matrix;
    private final Weighter weighter;
    private final CoreNlpService nlpService;

    public VectorSpace(CoreNlpService nlpService) {
        this.nlpService = nlpService;
        weighter = new Weighter();
        matrix = new DocumentTerm();
        matrix.addDocuments(weighter);
    }

    public void query(String query) {
        Vector q = queryToVector(query);

        List<Document> results = matrix.processQuery(q);

        if (!results.isEmpty()) {

            for (int i = 0; i < 10; i++) {
                System.out.println(results.get(i));
            }
        }

    }

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
