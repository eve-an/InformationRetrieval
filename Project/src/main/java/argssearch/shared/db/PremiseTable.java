package argssearch.shared.db;

public class PremiseTable extends AbstractTextTable {

  private static final String tableName = "premise";

  private static final String primaryPrimaryKeyAttributeName = "pid";
  private static final String titleAttributeName = "title";

  public PremiseTable() {
    super(tableName, primaryPrimaryKeyAttributeName);
  }

  @Override
  public String getTextAttributeName() {
    return PremiseTable.titleAttributeName;
  }
}
