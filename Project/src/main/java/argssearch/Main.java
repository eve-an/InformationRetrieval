package argssearch;

import argssearch.entity.ArgumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        JsonReader reader = new JsonReader();
        ArgumentList list = reader.read("/home/ivan/Downloads/idebate.json");
        if (list != null) {
            logger.info("Read {} arguments", list.getArguments().size());
        }
    }
}
