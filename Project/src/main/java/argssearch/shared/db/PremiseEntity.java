package argssearch.shared.db;

public class PremiseEntity extends AbstractTextEntity {

  private static final String tableName = "premise";

  private static final String primaryPrimaryKeyAttributeName = "pid";
  private static final String titleAttributeName = "title";

  public PremiseEntity() {
    super(tableName, primaryPrimaryKeyAttributeName);
  }

  @Override
  public String getTextAttributeName() {
    return PremiseEntity.titleAttributeName;
  }
}
