package argssearch.retrieval.models.vectorspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Vector {

    private List<Double> vector;
    private int docId;

    public Vector() {
        new ArrayList<>();
    }

    public Vector(int size, int docId) {
        vector = new ArrayList<>(Collections.nCopies(size, 0.0));
        this.docId = docId;
    }

    public Vector(List<Double> vector) {
        this.vector = vector;
    }

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
