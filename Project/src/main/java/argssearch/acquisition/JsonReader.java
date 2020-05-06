package argssearch.acquisition;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

public class JsonReader {

  private static JsonReader instance;

  public static JsonReader getInstance() {
    if (instance == null) {
      instance = new JsonReader();
    }

    return instance;
  }

  private final Gson gson;


  private JsonReader() {
    this.gson = new Gson();
  }

  public void read(final String path) {
    File json = new File(path);

    if (!json.exists()) {
      System.err.println("Json not found");
      System.exit(-1);
    }

    try (com.google.gson.stream.JsonReader jsonReader = new com.google.gson.stream.JsonReader(
        new FileReader(json))) {
      jsonReader.beginObject();
      jsonReader.nextName();
      jsonReader.beginArray();

      // Our Cache
      Stack<JsonArgument> arguments = new Stack<>();

      while (jsonReader.hasNext()) {

        if (arguments.size() == 50) {
          arguments.clear();
        }

        JsonArgument jsonArgument = gson.fromJson(jsonReader, JsonArgument.class);
        arguments.push(jsonArgument);
      }

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
