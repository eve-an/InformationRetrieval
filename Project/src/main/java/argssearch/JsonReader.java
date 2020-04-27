package argssearch;

import argssearch.entity.ArgumentList;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonReader {

    private static final Logger logger = LogManager.getLogger(JsonReader.class);

    /**
     * Read the args-me.json into POJOs. The file is long List of arguments.
     *
     * @param path Path to the json file.
     * @return A List of json files
     */
    public ArgumentList read(final String path) {
        File jsonFile = new File(path);

        if (!jsonFile.exists()) {
            logger.error("Did not found Json-File.");
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArgumentList argumentList = null;
        try {
            argumentList = mapper.readValue(jsonFile, ArgumentList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return argumentList;
    }
}
