package argssearch.acquisition;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;

class JsonProducer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JsonProducer.class);
    private final BlockingDeque<JsonArgument> queue;
    private final File jsonFile;
    private final Gson gson;


    public JsonProducer(final String path, final BlockingDeque<JsonArgument> queue) {
        jsonFile = new File(path);

        if (!jsonFile.exists()) {
            throw new RuntimeException("Could not find Json " + path);
        }
        this.queue = queue;
        this.gson = new Gson();
    }

    public void produce() {
        try (JsonReader jsonReader = new JsonReader(new FileReader(jsonFile))) {
            jsonReader.beginObject();
            jsonReader.nextName();
            jsonReader.beginArray();


            while (jsonReader.hasNext()) {
                JsonArgument jsonArgument = gson.fromJson(jsonReader, JsonArgument.class);

                try {
                    queue.put(jsonArgument);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        produce();
    }
}
