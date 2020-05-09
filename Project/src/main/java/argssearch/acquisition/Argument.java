package argssearch.acquisition;


class Argument {

    private long argid;
    private long pid;
    private String crawlid;
    private String content;
    private long totaltokens;
    private boolean ispro;

    public Argument(JsonArgument argument) {
        crawlid = argument.getId();
        content = argument.getConclusion();
        ispro = argument.getPremises().get(0).getStance().equalsIgnoreCase("PRO");
    }

    public long getArgid() {
        return argid;
    }

    public void setArgid(long argid) {
        this.argid = argid;
    }


    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }


    public String getCrawlid() {
        return crawlid;
    }

    public void setCrawlid(String crawlid) {
        this.crawlid = crawlid;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public long getTotaltokens() {
        return totaltokens;
    }

    public void setTotaltokens(long totaltokens) {
        this.totaltokens = totaltokens;
    }


    public boolean getIspro() {
        return ispro;
    }

    public void setIspro(boolean ispro) {
        this.ispro = ispro;
    }

}
