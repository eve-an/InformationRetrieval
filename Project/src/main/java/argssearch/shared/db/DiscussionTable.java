package argssearch.shared.db;

public class DiscussionTable extends AbstractTextTable {

  private static final String tableName = "discussion";

  private static final String primaryPrimaryKeyAttributeName = "did";
  private static final String titleAttributeName = "title";

  public DiscussionTable() {
    super(tableName, primaryPrimaryKeyAttributeName);
  }

  @Override
  public String getTextAttributeName() {
    return DiscussionTable.titleAttributeName;
  }
}
