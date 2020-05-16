package argssearch.shared.db;

public class ArgumentEntity extends AbstractTextEntity {
  private static final String tableName = "argument";

  private static final String primaryPrimaryKeyAttributeName = "argid";
  private static final String textAttributeName = "content";

  public ArgumentEntity() {
    super(tableName, primaryPrimaryKeyAttributeName);
  }

  @Override
  public String getTextAttributeName() {
    return ArgumentEntity.textAttributeName;
  }
}
