package argssearch;

import argssearch.entity.ArgumentList;

public class Main {

    public static void main(String[] args) {
        JsonReader reader = new JsonReader();
        ArgumentList list = reader.read("/home/ivan/Documents/args-me.json");
        if (list != null) {
            list.getArguments().forEach(System.out::println);
        }
    }
}
