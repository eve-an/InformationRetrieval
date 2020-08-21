package argssearch;



import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import argssearch.shared.util.ArgumentParser;
import executors.ParameterRunExecutor;
import executors.SingleMultiRunExecutor;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /* TODO: Extract args; Idea:
        First Arg = JDBC URL for Docker => IMPORTANT: Credentials moved to db.properties file
        Second Arg = Query Input path
        Third Arg = Query Output path
    */
    public static void main(String[] args) throws IOException {
        System.out.println(Arrays.toString(args));
        ArgumentParser argParser = new ArgumentParser();
        argParser
            .addStringArg("parameterRun", "pr", "Should a parameter run be performed (fromMultiplier:toMultiplier:stepSize) ie. (0:3:0.2)")
            .addStringArg("singleMultiRun", "smr" , "Should a single multi run be performed (discussionMultiplier:premiseMultiplier:argumentMultiplier) ie. (1:1:1)")
            .addStringArg("inputDirectory", "i" , "Input directory containing all files")
            .addStringArg("outputDirectory", "o", "The directory that should contain the outputs")
            .addStringArg("testDirectory", "t", "Where the run files will go")
            .addStringArg("skipReadingCrawl", "skip", "Dont read the crawled data in");

        argParser.parseArgs(args);


        // Save outputDirectory path
        String path = argParser.getString("testDirectory") ;
        if (!path.endsWith("/")) path += "/";
        path += "outputPath.txt";
        File f = new File(path);
        f.mkdirs();
        
        Files.write(Paths.get(path), argParser.getString("outputDirectory").getBytes());

        if (argParser.getString("parameterRun") != null
            &&
            !argParser.getString("parameterRun").toLowerCase().isEmpty()) {
            // Extract the specified values
            String[] values = argParser.getString("parameterRun").split("_");
            if (values.length != 3) {
                throw new RuntimeException("ParameterRun needs to be specified (fromMultiplier|toMultiplier|stepSize");
            }

            ParameterRunExecutor.MultiplierRun(
                argParser.getString("inputDirectory"),
                "~/TEST", //"~/TEST";
                argParser.getString("skipReadingCrawl") != null && argParser.getString("skipReadingCrawl").equals("true"),
                Double.parseDouble(values[0]),
                Double.parseDouble(values[1]),
                Double.parseDouble(values[2]));
        }
        if (argParser.getString("singleMultiRun") != null
            &&
            !argParser.getString("singleMultiRun").isEmpty()) {
            // Extract the specified multiplier
            String[] values = argParser.getString("singleMultiRun").split("_");
            if (values.length != 3) {
                throw new RuntimeException("ParameterRun needs to be specified (fromMultiplier|toMultiplier|stepSize");
            }

            SingleMultiRunExecutor.Compare(
                argParser.getString("inputDirectory"),
                "~/TEST",
                argParser.getString("skipReadingCrawl") != null && argParser.getString("skipReadingCrawl").equals("true"),
                Double.parseDouble(values[0]),
                Double.parseDouble(values[1]),
                Double.parseDouble(values[2])
            );
        }
    }
}
