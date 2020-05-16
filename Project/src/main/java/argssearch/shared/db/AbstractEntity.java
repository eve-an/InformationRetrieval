package argssearch.shared.db;

public abstract class AbstractEntity {
  private final String tableName;
  private final String primaryKeyAttributeName;

  public AbstractEntity(final String tableName, final String primaryKeyAttributeName) {
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
