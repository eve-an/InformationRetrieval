package argssearch.testexec;

import argssearch.retrieval.models.ConjunctiveRetrievalOnAllTables;
import argssearch.shared.cache.TokenCache;
import argssearch.shared.nlp.CoreNlpService;
import argssearch.shared.query.Topic;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConjunctiveMultipleParameterRun {

  private static final String outputTemplate = "%d Q0 %d %f Montalet\n";

  public ConjunctiveMultipleParameterRun(final CoreNlpService service, final TokenCache cache, final String outputFolderDir, final Topic t, final int bounds) {
    ConjunctiveRetrievalOnAllTables ret = new ConjunctiveRetrievalOnAllTables(service, cache);
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    for (int disMul = -bounds; disMul <= bounds; disMul++) {
      for (int premMul = -bounds; premMul <= bounds; premMul++) {
        for (int argMul = -bounds; argMul <= bounds; argMul++) {
          String path = outputFolderDir + this.getClass().getName() + String.format("%d-%d-%d", argMul, premMul, disMul);
          File f = new File(path);
          try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            ret.execute(
                t.getTitle(),
                0,
                0,
                0,
                disMul,
                premMul,
                argMul,
                50,
                50,
                50,
                50,
                (id, rank, weight) -> {
                  bw.write(String.format(outputTemplate, t.getNumber(), rank, weight))
                });
          } catch (IOException ioe) {
            logger.error("Failed writing to file", ioe);
          }
        }
      }
    }
  }
}
