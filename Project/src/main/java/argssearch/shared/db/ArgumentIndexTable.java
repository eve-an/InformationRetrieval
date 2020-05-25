package argssearch.shared.db;

public class ArgumentIndexTable extends AbstractIndexTable {

  private static final String tableName = "argument_index";
  private static final String refId = "argid";

  public ArgumentIndexTable() {
    super(tableName, refId);
  }
}
