package argssearch.shared.query;

/**
 * Class to represent the Result of a query. It follows the TREC format as stated here:
 * https://events.webis.de/touche-20/shared-task-1.html#submission
 */
public class Result {
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
    public String toString() {
        return "Result{" +
                "topicNumber=" + topicNumber +
                ", unusedField='" + unusedField + '\'' +
                ", documentId='" + documentId + '\'' +
                ", rank=" + rank +
                ", score=" + score +
                ", group='" + group + '\'' +
                '}';
    }
}
