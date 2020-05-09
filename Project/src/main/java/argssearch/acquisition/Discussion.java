package argssearch.acquisition;


import java.util.Objects;

class Discussion {

    private long did;
    private long sourceid;
    private String crawlid;
    private String title;
    private String url;

    public Discussion(JsonArgument argument) {
        crawlid = argument.getContext().getSourceId();
        title = argument.getContext().getDiscussionTitle();
        url = argument.getContext().getSourceUrl();
    }

    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }


    public long getSourceid() {
        return sourceid;
    }

    public void setSourceid(long sourceid) {
        this.sourceid = sourceid;
    }


    public String getCrawlid() {
        return crawlid;
    }

    public void setCrawlid(String crawlid) {
        this.crawlid = crawlid;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        return crawlid.equals(that.crawlid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crawlid);
    }
}
