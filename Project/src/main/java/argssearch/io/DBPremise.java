package argssearch.io;


import java.util.StringTokenizer;

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

    public int getLength() {
        return new StringTokenizer(title).countTokens();
    }
}
