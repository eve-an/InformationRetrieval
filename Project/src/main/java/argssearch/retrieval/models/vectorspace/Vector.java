package argssearch.retrieval.models.vectorspace;


import java.util.HashMap;
import java.util.Map;

/**
 * A Vector represents a term or document.
 * Its entries are weights of the tokens.
 */
public class Vector {

    private Double norm2;
    private final Map<Integer, Double> weights;

    public Vector() {
        weights = new HashMap<>();
    }

    public Vector(Map<Integer, Double> weights) {
        this.weights = weights;
    }

    /**
     * Read the weights for the token id into a map
     */
    public void read(Integer[] tokenIds, Double[] weights) {
        if (tokenIds.length != weights.length) {
            throw new IllegalStateException("Arrays must be of same size.");
        }

        for (int i = 0; i < tokenIds.length; i++) {
            this.weights.put(tokenIds[i] - 1, weights[i]);
        }
    }

    /**
     * Computes the euclidean norm of this vector which is geometrically the length of the vector.
     *
     * @return euclidean norm OR length of vector
     */
    private double norm() {
        double normQuad = weights.values().stream()
                .map(x -> x * x)
                .reduce(Double::sum)
                .orElseThrow(() -> new IllegalStateException("Weights Map cannot be empty"));

        return Math.sqrt(normQuad);
    }

    public void set(int index, double weight) {
        this.weights.put(index, weight);
    }

    private double dotProduct(Vector other) {
        double sum = 0;
        for (Map.Entry<Integer, Double> entry : other.weights.entrySet()) {
            int id = entry.getKey();
            double weight = entry.getValue();

            if (weights.containsKey(id)) {
                sum += weight * weights.get(id);
            }
        }

        return sum;
    }

    public double getCosineSimilarity(Vector other) {
        double lnorm = other.getNorm2();
        if (lnorm == 0.0) return 0.0;
        double rnorm = this.getNorm2();
        if (rnorm == 0.0) return 0.0;

        return this.dotProduct(other) / (rnorm * lnorm);
    }

    public boolean isEmpty() {
        return weights.isEmpty();
    }


    public double getNorm2() {
        if (norm2 == null) {
            norm2 = norm();
        }
        return norm2;
    }
}
