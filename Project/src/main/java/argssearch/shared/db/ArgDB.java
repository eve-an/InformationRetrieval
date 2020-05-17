package argssearch.shared.db;

import java.sql.*;

public class ArgDB {

	private static class InstanceHolder {
		private static ArgDB argDBInstance;
	}
	public static ArgDB getInstance() {
		if (InstanceHolder.argDBInstance == null) {
			InstanceHolder.argDBInstance = new ArgDB();
		}
		return InstanceHolder.argDBInstance;
	}


	private Connection conn;
	private ArgDB() {
		try {
			this.conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/argdb", "postgres",
					"robin");
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public Connection getConnection(){
		return conn;
	}
	
	public void dbInsert(String string) {
		//TODO
	}

	public PreparedStatement prepareStatement(String query) {
		try {
			return this.conn.prepareStatement(query);
		} catch (SQLException sqlE) {
			// TODO log this
			sqlE.printStackTrace();
		}
		return null;
	}

	public PreparedStatement prepareStatementWithReturnOfId(String query, String primaryKeyAttributeName) {
		try {
			return this.conn.prepareStatement(query, new String[]{primaryKeyAttributeName});
		} catch (SQLException sqlE) {
			// TODO log this
			sqlE.printStackTrace();
		}
		return null;
	}

	public ResultSet query(String queryText) {
		try {
			return this.conn.createStatement().executeQuery(queryText);
		} catch(SQLException sqlE) {
			// TODO log
		}
		return null;
	}

	public static boolean isException(SQLException sqle) {
		return false;
	}
}

