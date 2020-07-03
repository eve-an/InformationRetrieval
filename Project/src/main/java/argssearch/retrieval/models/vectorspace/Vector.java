package argssearch.retrieval.models.vectorspace;


/**
 * A Vector represents a term or document.
 * Its entries are weights of the tokens.
 */
public class Vector {

    private final double[] vector;    // Store weights in a List
    private double norm2;

    /**
     * Constructs a Vector with given size and initialize it's weight with 0.
     *
     * @param size  number of elements of the vector
     */
    public Vector(int size) {
        vector = new double[size];
        norm2 = -1.0;
    }

    /**
     * Computes the euclidean norm of this vector which is geometrically the length of the vector.
     *
     * @return euclidean norm / length of vector
     */
    private double norm() {
        double sum = 0.0;
        for (double v : this.vector) {
            sum += v * v;
        }

        return Math.sqrt(sum);
    }

    public void set(int index, double weight) {
        vector[index] = weight;
    }

    public double get(int index) {
        return vector[index];
    }

    public int getSize() {
        return vector.length;
    }

    public double getNorm2() {
        if (norm2 == -1.0) {
            norm2 = norm();
        }
        return norm2;
    }
}
