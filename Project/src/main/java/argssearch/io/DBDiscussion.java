package argssearch.io;


import argssearch.shared.nlp.CoreNlpService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.StringTokenizer;

class DBDiscussion {

    private final String crawlId;
    private final String title;
    private final String url;

    public DBDiscussion(JsonArgument argument) {
        crawlId = argument.getContext().getSourceId();
        title = argument.getContext().getDiscussionTitle();
        url = argument.getContext().getSourceUrl();
    }

    public String getCrawlId() {
        return crawlId;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DBDiscussion that = (DBDiscussion) o;
        return crawlId.equals(that.crawlId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crawlId);
    }

    public int getLength() {
        try {
            return new CoreNlpService().getWordCount(title);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
