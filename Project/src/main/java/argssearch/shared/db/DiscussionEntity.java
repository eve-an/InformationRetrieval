package argssearch.shared.db;

public class DiscussionEntity extends AbstractTextEntity {

  private static final String tableName = "discussion";

  private static final String primaryPrimaryKeyAttributeName = "did";
  private static final String titleAttributeName = "title";

  public DiscussionEntity() {
    super(tableName, primaryPrimaryKeyAttributeName);
  }

  @Override
  public String getTextAttributeName() {
    return DiscussionEntity.titleAttributeName;
  }
}
