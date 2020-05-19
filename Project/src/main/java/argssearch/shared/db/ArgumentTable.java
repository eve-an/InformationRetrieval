package argssearch.shared.db;

public class ArgumentTable extends AbstractTextTable {
  private static final String tableName = "argument";

  private static final String primaryPrimaryKeyAttributeName = "argid";
  private static final String textAttributeName = "content";

  public ArgumentTable() {
    super(tableName, primaryPrimaryKeyAttributeName);
  }

  @Override
  public String getTextAttributeName() {
    return ArgumentTable.textAttributeName;
  }
}
