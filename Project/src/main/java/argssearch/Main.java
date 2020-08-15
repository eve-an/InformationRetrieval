
package argssearch;

import argssearch.retrieval.models.ModelType;
import argssearch.shared.db.ArgDB;
import argssearch.shared.query.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /* TODO: Extract args; Idea:
        First Arg = JDBC URL for Docker
        Second Arg = Query Input path
        Third Arg = Query Output path
    */
    public static void main(String[] args) {
        logger.info("Starting ArgsSearch...");

        if (args.length == 1) {
            ArgDB.getInstance().connectToDB(args[0]);
        } else {
            ArgDB.getInstance().connectToDB();
        }

        logger.info("Connected to DB.");

        // Example Topic from Touche
        // https://events.webis.de/touche-20/shared-task-1.html#submission
        String exampleTitle = "Is climate change real?";
        String exampleDescription = "You read an opinion piece on how climate change is a hoax and disagree. " +
                "Now you are looking for arguments supporting the claim that climate change is in fact real.";
        String exampleNarrative = "Relevant arguments will support the given stance that climate change is real " +
                "or attack a hoax side's argument.";

        Topic example = new Topic(1, exampleTitle, exampleDescription, exampleNarrative);

        //Pipeline pipeline = new Pipeline(example, "/path/to/json_directory");
        Pipeline pipeline = new Pipeline(example);

        pipeline.exec(ModelType.VECTOR_SPACE);  // Retrieve Documents
    }
}