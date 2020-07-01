package argssearch.shared.query;


import java.util.Objects;

/**
 * Class to represent the Result of a query. It follows the TREC format as stated here:
 * https://events.webis.de/touche-20/shared-task-1.html#submission
 */
public class Result implements Comparable<Result> {
    private final int topicNumber;
    private final String unusedField = "Q0";
    private final String documentId;
    private final int rank;
    private final double score;
    private final String group = "Montalet";

    public Result(int topicNumber, String documentId, int rank, double score) {
        this.topicNumber = topicNumber;
        this.documentId = documentId;
        this.rank = rank;
        this.score = score;
    }

    public int getTopicNumber() {
        return topicNumber;
    }

    public String getUnusedField() {
        return unusedField;
    }

    public String getDocumentId() {
        return documentId;
    }

    public int getRank() {
        return rank;
    }

    public double getScore() {
        return score;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return documentId.equals(result.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }

    /**
     * Override this function to sort the Results in descending order by their rank
     *
     * @param result other
     * @return see Double.compare()
     */
    @Override
    public int compareTo(Result result) {
        return Double.compare(result.rank, this.rank);
    }

    /**
     * @return formatted String which follows the TREC format
     */
    @Override
    public String toString() {
        return String.format("%d %s %s %d %.2f %s", topicNumber, unusedField, documentId, rank, score, group);
    }
}
