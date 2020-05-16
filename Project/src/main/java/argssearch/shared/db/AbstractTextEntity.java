package argssearch.shared.db;

public abstract class AbstractTextEntity extends AbstractEntity{

  public AbstractTextEntity(String tableName, String primaryKeyAttributeName) {
    super(tableName, primaryKeyAttributeName);
  }

  public abstract String getTextAttributeName();
}
