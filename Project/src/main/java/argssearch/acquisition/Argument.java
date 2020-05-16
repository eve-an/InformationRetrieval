package argssearch.acquisition;


class Argument {

    private final String crawlId;
    private final String content;
    private final boolean isPro;

    public Argument(JsonArgument argument) {
        crawlId = argument.getId();
        content = argument.getConclusion();
        isPro = argument.getPremises().get(0).getStance().equalsIgnoreCase("PRO");
    }

    public String getCrawlId() {
        return crawlId;
    }

    public String getContent() {
        return content;
    }

    public boolean isPro() {
        return isPro;
    }

}
