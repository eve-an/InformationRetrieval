package argssearch;



import argssearch.shared.util.ArgumentParser;
import executors.ParameterRunExecutor;
import executors.SingleMultiRunExecutor;
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
        ArgumentParser argParser = new ArgumentParser();
        argParser
            .addStringArg("parameterRun", "pr", "Should a parameter run be performed (fromMultiplier:toMultiplier:stepSize) ie. (0:3:0.2)")
            .addStringArg("singleMultiRun", "smr" , "Should a single multi run be performed (discussionMultiplier:premiseMultiplier:argumentMultiplier) ie. (1:1:1)")
            .addStringArg("inputDirectory", "i" , "The path to the topic file")
            .addStringArg("outputDirectory", "o", "The directory that should contain the outputs")
            .addStringArg("jsonDirectory", "j", "The path of the json file containing the crawled data");

        argParser.parseArgs(args);

        if (argParser.getString("parameterRun") != null
            &&
            !argParser.getString("parameterRun").toLowerCase().isEmpty()) {
            // Extract the specified values
            String[] values = argParser.getString("parameterRun").split(":");
            if (values.length != 3) {
                throw new RuntimeException("ParameterRun needs to be specified (fromMultiplier|toMultiplier|stepSize");
            }

            ParameterRunExecutor.WeightRun(
                argParser.getString("inputDirectory"),
                argParser.getString("outputDirectory"),
                argParser.getString("jsonDirectory"),
                Double.parseDouble(values[0]),
                Double.parseDouble(values[1]),
                Double.parseDouble(values[2]));
        }
        if (argParser.getString("singleMultiRun") != null
            &&
            !argParser.getString("singleMultiRun").isEmpty()) {
            // Extract the specified multiplier
            String[] values = argParser.getString("singleMultiRun").split(":");
            if (values.length != 3) {
                throw new RuntimeException("ParameterRun needs to be specified (fromMultiplier|toMultiplier|stepSize");
            }

            SingleMultiRunExecutor.Compare(
                argParser.getString("inputDirectory"),
                argParser.getString("outputDirectory"),
                argParser.getString("jsonDirectory"),
                Double.parseDouble(values[0]),
                Double.parseDouble(values[1]),
                Double.parseDouble(values[2])
            );
        }
    }
}
