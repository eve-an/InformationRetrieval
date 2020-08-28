package argssearch.io;


import argssearch.shared.nlp.CoreNlpService;

import java.io.IOException;
import java.io.UncheckedIOException;

class DBArgument {

    private final String crawlId;
    private final String content;
    private final boolean isPro;

    public DBArgument(JsonArgument argument) {
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
            return new CoreNlpService().getWordCount(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
