package argssearch.io;


import argssearch.shared.nlp.CoreNlpService;

import java.io.IOException;
import java.util.StringTokenizer;

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

    public int getLength() {

        try {
            int count = new CoreNlpService().getWordCount(content);
            System.out.println("Count = " + count + " - " + content + " - " + crawlId);
            return count;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
