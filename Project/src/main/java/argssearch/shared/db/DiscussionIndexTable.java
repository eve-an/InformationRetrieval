package argssearch.shared.db;

public class DiscussionIndexTable extends AbstractIndexTable {

  private static final String tableName = "discussion_index";
  private static final String refId = "did";

  public DiscussionIndexTable() {
    super(tableName, refId);
  }
}
