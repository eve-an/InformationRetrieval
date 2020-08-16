package argssearch.io;


class Premise {

    private final String crawlId;
    private final String title;

    public Premise(JsonArgument argument) {
        crawlId = argument.getId();
        title = argument.getPremises().get(0).getText();
    }

    public String getCrawlId() {
        return crawlId;
    }

    public String getTitle() {
        return title;
    }

}
