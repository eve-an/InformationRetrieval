package argssearch.shared.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
			this.conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/argdb", "irargdb",
					"");
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
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

	public ResultSet query(String queryText) {
		try {
			return this.conn.createStatement().executeQuery(queryText);
		} catch(SQLException sqlE) {
			// TODO log
		}
		return null;
	}
}

