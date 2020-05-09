package argssearch.shared.db;

public abstract class AbstractTable {
  private final String tableName;
  private final String primaryKeyAttributeName;

  public AbstractTable(final String tableName, final String primaryKeyAttributeName) {
    this.tableName = tableName;
    this.primaryKeyAttributeName = primaryKeyAttributeName;
  }

  public String getTableName() {
    return tableName;
  }

  public String getPrimaryKeyAttributeName() {
    return primaryKeyAttributeName;
  }
}
