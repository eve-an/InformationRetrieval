package argssearch.retrieval.models.vectorspace;

/**
 * Math class for vector operations
 */
public class VectorMath {

    /**
     * Scalar product of two vectors. Vectors need to be of same size.
     *
     * @param l left vector
     * @param r right vector
     * @return sum of products of corresponding entries
     */
    public static double dotProduct(final Vector l, final Vector r) {
        if (l.getSize() != r.getSize()) {
            throw new ArrayIndexOutOfBoundsException("Vectors must be of same size.");
        }

        double sum = 0.0;
        for (int i = 0; i < l.getSize(); i++) {
            sum += l.get(i) * r.get(i);
        }

        return sum;
    }

    /**
     * Computes the cosine similarity of two vectors.
     * See: https://en.wikipedia.org/wiki/Vector_space_model#Applications
     *
     * @param l left vector
     * @param r right vector
     * @return cosine similarity of two vectors.
     */
    public static double getCosineSimilarity(final Vector l, final Vector r) {
        if (l.getSize() != r.getSize()) {
            throw new ArrayIndexOutOfBoundsException("Vectors must be of same size.");
        }

        double lnorm = l.getNorm2();
        if (lnorm == 0.0) return 0.0;
        double rnorm = r.getNorm2();
        if (rnorm == 0.0) return 0.0;

        return dotProduct(l, r) / (rnorm * lnorm);
    }
}
