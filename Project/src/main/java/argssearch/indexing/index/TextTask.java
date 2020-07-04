package argssearch.indexing.index;

class TextTask {
  private final int id;
  private final String text;

  TextTask(int id, String text) {
    this.id = id;
    this.text = text;
  }

  int getId() {
    return id;
  }

  String getText() {
    return text;
  }
}
