package argssearch.acquisition;


import java.util.Objects;

class Discussion {

    private final String crawlId;
    private final String title;
    private final String url;

    public Discussion(JsonArgument argument) {
        crawlId = argument.getContext().getSourceId();
        title = argument.getContext().getDiscussionTitle();
        url = argument.getContext().getSourceUrl();
    }

    public String getCrawlId() {
        return crawlId;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Discussion that = (Discussion) o;
        return crawlId.equals(that.crawlId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crawlId);
    }
}
