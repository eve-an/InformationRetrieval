package argssearch.io;


import argssearch.shared.nlp.CoreNlpService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.StringTokenizer;

class DBPremise {

    private final String crawlId;
    private final String title;

    public DBPremise(JsonArgument argument) {
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
        try {
            return new CoreNlpService().getWordCount(title);
        } catch (IOException e) {
           throw new UncheckedIOException(e);
        }
    }
}
