package argssearch.retrieval.models.vectorspace;

/**
 * Math class for vector operations
 */
public class VectorMath {

    /**
     * Computes the cosine similarity of two vectors.
     * See: https://en.wikipedia.org/wiki/Vector_space_model#Applications
     *
     * @param l left vector
     * @param r right vector
     * @return cosine similarity of two vectors.
     */
    public static double getCosineSimilarity(final Vector l, final Vector r) {
        double lnorm = l.getNorm2();
        if (lnorm == 0.0) return 0.0;
        double rnorm = r.getNorm2();
        if (rnorm == 0.0) return 0.0;

        return l.dotProduct(r) / (rnorm * lnorm);
    }
}
