package executors;

import argssearch.Pipeline;
import argssearch.io.XmlParser;
import argssearch.retrieval.models.ModelType;
import argssearch.shared.db.AbstractIndexTable;
import argssearch.shared.db.ArgumentIndexTable;
import argssearch.shared.db.DiscussionIndexTable;
import argssearch.shared.db.PremiseIndexTable;
import argssearch.shared.query.Topic;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SingleMultiRunExecutor {

  public static void Compare(
      String topicFile,
      String destFolder,
      final String pathToJsonDir,
      final double dMultiplier,
      final double pMultiplier,
      final double aMultiplier) throws IOException {

    // Check parameters
    if (destFolder.endsWith("/")) {
      destFolder = destFolder.substring(0, destFolder.length()-1);
    }

    Pipeline pipeline = new Pipeline(pathToJsonDir);
    for (Topic topic : XmlParser.from(topicFile)) {
      pipeline.setTopic(topic);
      for (ModelType model : ModelType.values()) {
        // MULTI
        {
          // Create path based on model name and parameters
          String filePath = String.format(
              "%s/%d/%s/Multi.txt",
              destFolder,
              topic.getNumber(),
              model.name()
          );

          File f = new File(filePath);
          // Create folder hierarchy
          f.getParentFile().mkdirs();

          // Create a writer specific to this run
          try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            // execute the search with the parameters defined by this run
            pipeline.execMulti(model, dMultiplier, pMultiplier, aMultiplier, result -> {

              // Write to the result file
              try {
                writer.write(result.toTREC() + "\n");
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }

        // SINGLE
        {
          for (AbstractIndexTable indexTable : List
              .of(new ArgumentIndexTable(), new PremiseIndexTable(),
                  new DiscussionIndexTable())) {
            // Create path based on model name and parameters
            String filePath = String.format(
                "%s/%d/%s/%s.txt",
                destFolder,
                topic.getNumber(),
                model.name(),
                indexTable.getClass().getSimpleName()
            );
            File f = new File(filePath);
            // Create folder hierarchy
            f.getParentFile().mkdirs();

            // Create a writer specific to this run
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
              // execute the search with the parameters defined by this run
              pipeline.execSingle(model, indexTable, result -> {

                // Write to the result file
                try {
                  writer.write(result.toTREC() + "\n");
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
            } catch (SQLException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
  }
}
