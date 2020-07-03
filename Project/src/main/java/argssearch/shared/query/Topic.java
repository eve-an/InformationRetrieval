package argssearch.shared.query;

/**
 * Topic given by Touche. This class will be parsed from XML-File.
 * https://events.webis.de/touche-20/shared-task-1.html#submission
 */
public class Topic {
    private final int number;
    private final String title;
    private final String description;
    private final String narrative;

    public Topic(int number, String title, String description, String narrative) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.narrative = narrative;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getNarrative() {
        return narrative;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "number=" + number +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", narrative='" + narrative + '\'' +
                '}';
    }
}
