package argssearch.retrieval.models.vectorspace;

import java.util.Objects;

public class Document implements Comparable<Document> {
    private int id;
    private double rank;

    public Document(int id, double rank) {
        this.id = id;
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return id == document.id &&
                Double.compare(document.rank, rank) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rank);
    }

    @Override
    public int compareTo(Document document) {
        return Double.compare(document.rank, this.rank);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", rank=" + rank +
                '}';
    }
}
