package argssearch;




import argssearch.indexing.index.Indexer;
import argssearch.indexing.index.TFIDFWeighter;
import argssearch.io.Acquisition;
import argssearch.shared.cache.TokenCachePool;
import argssearch.shared.db.ArgDB;
import argssearch.shared.nlp.CoreNlpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        ArgDB.getInstance().dropSchema("public");
        ArgDB.getInstance().dropSchema("temp");
        ArgDB.getInstance().createSchema();
        Acquisition.exec("/home/ivan/Documents/IR_JSONS", new LinkedBlockingDeque<>(16));
        Indexer.index(new CoreNlpService(), TokenCachePool.getInstance().get(Integer.MAX_VALUE));
        TFIDFWeighter.weigh();
    }
}
