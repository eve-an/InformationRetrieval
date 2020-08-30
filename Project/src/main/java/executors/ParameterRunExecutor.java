package executors;

import argssearch.Pipeline;
import argssearch.io.XmlParser;
import argssearch.retrieval.models.ModelType;
import argssearch.shared.query.Topic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterRunExecutor {

    public static void MultiplierRun(
            String inputDirectory,
            String destFolder,
            final boolean skipReadingCrawl,
            final double fromWeight,
            final double toWeight,
            final double stepSize) {
        // Check parameters
        if (destFolder.endsWith("/")) {
            destFolder = destFolder.substring(0, destFolder.length() - 1);
        }
        if (!inputDirectory.endsWith("/")) inputDirectory += "/";


        try (Pipeline pipeline = new Pipeline(inputDirectory, skipReadingCrawl)) {
            for (Topic topic : XmlParser.from(inputDirectory + "topics.xml")) {
                pipeline.setTopic(topic);

                for (double dWeight = fromWeight; dWeight <= toWeight; dWeight += stepSize) {
                    for (double pWeight = fromWeight; pWeight <= toWeight; pWeight += stepSize) {
                        for (double aWeight = fromWeight; aWeight <= toWeight; aWeight += stepSize) {
                            // Create path based on model name and parameters
                            String fileName = String.format(Locale.ENGLISH,
                                    "%s/%d/%s/%f-%f-%f.txt",
                                    destFolder,
                                    topic.getNumber(),
                                    ModelType.VECTOR_SPACE.name(),
                                    dWeight,
                                    pWeight,
                                    aWeight);
                            File f = new File(fileName);
                            // Create folder hierarchy
                            f.getParentFile().mkdirs();

                            // Create a writer specific to this run
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
                                // execute the search with the parameters defined by this run
                                pipeline.execMulti(ModelType.VECTOR_SPACE, dWeight, pWeight, aWeight, result -> {

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
        } catch (Exception e) {
            logger.error("Error in parameter run", e);
        }
    }

    private static Logger logger = null;

    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(ParameterRunExecutor.class);
        }
        return logger;
    }
}
