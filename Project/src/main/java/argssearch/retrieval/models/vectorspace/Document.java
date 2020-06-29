package argssearch.retrieval.models.vectorspace;

import java.util.Objects;

/**
 * POJO to store Document information. Used for ranking.
 */
public class Document implements Comparable<Document> {

    enum Type {
        ARGUMENT, PREMISE, DISCUSSION
    }

    private int id;
    private Type type;
    private double rank;

    public Document(int id, double rank, Type type) {
        this.id = id;
        this.rank = rank;
        this.type = type;
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
                Double.compare(document.rank, rank) == 0 &&
                type == document.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, rank);
    }

    /**
     * This document is less when his rank is smaller than the other's document rank.
     *
     * @param document other document
     * @return -1 when less, 0 when equal, 1 when greater.
     */
    @Override
    public int compareTo(Document document) {
        return Double.compare(document.rank, this.rank);
    }

    @Override
    public String toString() {
        return "Document{" +
                "type=" + type.toString() +
                ", id=" + id +
                ", rank=" + rank +
                '}';
    }
}
