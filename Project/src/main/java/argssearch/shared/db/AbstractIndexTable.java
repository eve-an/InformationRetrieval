package argssearch.shared.db;

public abstract class AbstractIndexTable {

  private static final String tokenID = "tid";
  private static final String weight = "weight";
  private static final String occurrences = "occurrences";
  private static final String offsets = "offsets";

  private final String tableName;
  private final String refId;

  /**
   * @param tableName name of the table
   * @param refId the attribute name of table that is referenced (argId for the argument table)
   * */
  public AbstractIndexTable(final String tableName, final String refId) {
    this.tableName = tableName;
    this.refId = refId;
  }

  public String getTokenID() {
    return tokenID;
  }

  public String getWeight() {
    return weight;
  }

  public String getOccurrences() {
    return occurrences;
  }

  public String getOffsets() {
    return offsets;
  }

  public String getTableName() {
    return tableName;
  }

  public String getRefId() {
    return refId;
  }
}
