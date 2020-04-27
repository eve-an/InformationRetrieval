package argssearch.entity;

public class Context {

    private String sourceId;
    private String previousArgumentInSourceId;
    private String acquisitionTime;
    private String discussionTitle;
    private String sourceTitle;
    private String sourceUrl;
    private String nextArgumentInSourceId;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getPreviousArgumentInSourceId() {
        return previousArgumentInSourceId;
    }

    public void setPreviousArgumentInSourceId(String previousArgumentInSourceId) {
        this.previousArgumentInSourceId = previousArgumentInSourceId;
    }

    public String getAcquisitionTime() {
        return acquisitionTime;
    }

    public void setAcquisitionTime(String acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public String getDiscussionTitle() {
        return discussionTitle;
    }

    public void setDiscussionTitle(String discussionTitle) {
        this.discussionTitle = discussionTitle;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getNextArgumentInSourceId() {
        return nextArgumentInSourceId;
    }

    public void setNextArgumentInSourceId(String nextArgumentInSourceId) {
        this.nextArgumentInSourceId = nextArgumentInSourceId;
    }

    @Override
    public String toString() {
        return "Context{" +
                "sourceId='" + sourceId + '\'' +
                ", previousArgumentInSourceId='" + previousArgumentInSourceId + '\'' +
                ", acquisitionTime='" + acquisitionTime + '\'' +
                ", discussionTitle='" + discussionTitle + '\'' +
                ", sourceTitle='" + sourceTitle + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", nextArgumentInSourceId='" + nextArgumentInSourceId + '\'' +
                '}';
    }
}
