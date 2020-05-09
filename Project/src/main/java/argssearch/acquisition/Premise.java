package argssearch.acquisition;


class Premise {

    private long pid;
    private long did;
    private String crawlid;
    private String title;

    public Premise(JsonArgument argument) {
        crawlid = argument.getId();
        title = argument.getPremises().get(0).getText();
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }


    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
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

}
