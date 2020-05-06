package argssearch;


import argssearch.acquisition.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    JsonReader.getInstance().read("/home/ivan/Documents/Information_Retrieval_Json/debateorg.json");
  }


}
