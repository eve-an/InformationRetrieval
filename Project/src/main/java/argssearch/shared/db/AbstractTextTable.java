package argssearch.shared.db;

public abstract class AbstractTextTable extends AbstractTable {

  public AbstractTextTable(String tableName, String primaryKeyAttributeName) {
    super(tableName, primaryKeyAttributeName);
  }

  public abstract String getTextAttributeName();
}
