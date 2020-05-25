package argssearch.shared.db;

public class PremiseIndexTable extends AbstractIndexTable {

  private static final String tableName = "premise_index";
  private static final String refId = "pid";

  public PremiseIndexTable() {
    super(tableName, refId);
  }
}
