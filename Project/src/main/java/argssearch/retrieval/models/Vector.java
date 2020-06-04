package argssearch.retrieval.models;

import java.util.ArrayList;
import java.util.List;


public class Vector {
    private List<Double> vector;

    public Vector() {
        new ArrayList<>();
    }

    public Vector(int size) {
        vector = new ArrayList<>(size);
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

    public double get(int index) {
        return vector.get(index);
    }

    public int getSize() {
        return vector.size();
    }
}
