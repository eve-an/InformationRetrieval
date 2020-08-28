package argssearch.retrieval.models.vectorspace;


import java.util.HashMap;
import java.util.Map;

/**
 * A Vector represents a term or document.
 * Its entries are weights of the tokens.
 */
public class Vector {

    private double norm2;
    private final Map<Integer, Double> weights;

    /**
     * Constructs a Vector with given size and initialize it's weight with 0.
     *
     */
    public Vector() {
        weights = new HashMap<>();
        norm2 = -1.0;
    }

    public void read(Integer[] tokenIds, Double[] weights) {
        for (int i = 0; i < tokenIds.length; i++) {
            this.weights.put(tokenIds[i] - 1, weights[i]);
        }
    }

    /**
     * Computes the euclidean norm of this vector which is geometrically the length of the vector.
     *
     * @return euclidean norm / length of vector
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

    public double dotProduct(Vector other) {
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


    public double getNorm2() {
        if (norm2 == -1.0) {
            norm2 = norm();
        }
        return norm2;
    }
}
