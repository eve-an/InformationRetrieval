package argssearch;

import argssearch.retrieval.models.ModelType;
import argssearch.shared.query.Topic;
import argssearch.shared.util.FileHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class Demo {

    public void demonstrate() throws IOException {
        Topic topic = new Topic(1, "women's rights", "", "");
        Path path = Files.createTempFile("parliamentary", ".json");
        Files.write(path, new FileHandler().getResourceAsString("/demo/demo.json").getBytes());

        //Pipeline pipeline = new Pipeline(topic, path.toAbsolutePath().normalize().toString());

        /*
        try {
            pipeline.exec(ModelType.VECTOR_SPACE);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }*/
    }
}
