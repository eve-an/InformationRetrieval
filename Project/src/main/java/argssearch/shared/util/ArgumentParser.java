package argssearch.shared.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class ArgumentParser {

  private static class Argument {
    private final String fullName;
    private final String shortName;
    private final String defaultValue;
    private String parsedValue;
    private final String description;

    public Argument(String fullName, String shortName, String defaultValue, String description) {
      this.fullName = fullName;
      this.shortName = shortName;
      this.defaultValue = defaultValue;
      this.description = description;
    }

    @Override
    public String toString() {
      return "--"+fullName + "\t" + "-" + shortName + "\t" + description;
    }
  }

  // mapping of name to argument
  private Map<String, Argument> argumentsByFullName;
  private Map<String, Argument> argumentsByShortName;

  private ArgumentParser() {
    this.argumentsByFullName = new LinkedHashMap<>();
    this.argumentsByShortName = new LinkedHashMap<>();
  }

  public ArgumentParser addStringArg(final String fullName, final String shortName, final String defaultValue, final String desc) {

    if (argumentsByFullName.containsKey(fullName) || argumentsByShortName.containsKey(shortName)) {
      this.printError("No duplicate Arguments allowed");
    }

    Argument a = new Argument(fullName, shortName, defaultValue, desc);
    this.argumentsByFullName.put(fullName, a);
    this.argumentsByShortName.put(shortName, a);
    return this;
  }

  public void parseArgs(String[] args) {
    if (args == null || args.length == 0) {
      this.printHelp();
    }
    if (args[0].toLowerCase().equals("help")) {
      this.printHelp();
    }
    if (args.length % 2 != 0) {
      this.printError("Arguments should be in shape > --ARG_NAME VALUE <");
    }

    // Get the grouped arguments, add two together
    String[] groupedArgs = new String[Math.floorDiv(args.length, 2)];
    for (int i = 0; i < args.length; i++) {
      int groupedIndex = Math.floorDiv(i, 2);
      String cur = groupedArgs[groupedIndex] == null ? "" : groupedArgs[groupedIndex];
      groupedArgs[groupedIndex] = cur + " " + args[i];
    }

    // go trough all pairs like: -?key value
    for (String arg : groupedArgs) {
      arg = arg.trim();

      // long arg
      if (arg.matches("--\\w+\\s[^\\s]+")) {
        String argumentFullName = arg.substring(2, arg.indexOf(' '));
        Argument a = argumentsByFullName.get(argumentFullName);
        if (a != null) {
          a.parsedValue = arg.substring(arg.indexOf(' ')+1);
          argumentsByFullName.put(argumentFullName, a);
        }
        continue;
      }
      // short arg
      if (arg.matches("-\\w+\\s[^\\s]+")) {
        String argumentShortName = arg.substring(1, arg.indexOf(' '));
        Argument a = argumentsByShortName.get(argumentShortName);
        if (a != null) {
          a.parsedValue = arg.substring(arg.indexOf(' ')+1);
          argumentsByShortName.put(argumentShortName, a);
        }
        continue;
      }
      this.printError("Malformed argument: " + arg);
    }
  }

  public String getString(final String name) {
    Argument a = this.argumentsByFullName.get(name);
    if (a == null) {
      return null;
    }
    return a.parsedValue != null  ? a.parsedValue : a.defaultValue;
  }

  /**
   * Get the builder object
   * */
  public static ArgumentParser build() {return new ArgumentParser();}

  private void printError(String msg) {
    System.out.println(msg);
    System.exit(1);
  }

  private void printHelp() {
    System.out.println("--ARG_NAME\t-ARG_ABBREV\tdescription");
    System.out.println("------------------------------------");
    argumentsByFullName.values().forEach(System.out::println);
    System.exit(1);
  }
}
