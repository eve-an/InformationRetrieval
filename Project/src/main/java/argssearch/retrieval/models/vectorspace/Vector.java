package argssearch.retrieval.models.vectorspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Vector represents a term or document.
 * Its entries are weights of the tokens.
 */
public class Vector {

    private final List<Double> vector;    // Store weights in a List
    private final int docId;

    /**
     * Constructs a Vector with given size and initialize it's weight with 0.
     * Also store the Id of the corresponding document.
     *
     * @param size  number of elements of the vector
     * @param docId document's id
     */
    public Vector(int size, int docId) {
        vector = new ArrayList<>(Collections.nCopies(size, 0.0));
        this.docId = docId;
    }

    /**
     * Computes the euclidean norm of this vector which is geometrically the length of the vector.
     *
     * @return euclidean norm / length of vector
     */
    public double norm() {
        double sum = 0;
        for (int i = 0; i < this.getSize(); i++) {
            sum += this.get(i) * this.get(i);
        }

        return Math.sqrt(sum);
    }

    public void set(int index, double weight) {
        vector.set(index, weight);
    }

    public double get(int index) {
        return vector.get(index);
    }

    public int getSize() {
        return vector.size();
    }

    public int getDocId() {
        return docId;
    }
}
