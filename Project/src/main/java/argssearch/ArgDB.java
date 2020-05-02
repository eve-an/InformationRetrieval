package argssearch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;

public class ArgDB {

	public ArgDB() {
		try {
			Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/argdb", "irargdb",
					"");
			Statement prep = conn.createStatement();
			ResultSet rs = prep.executeQuery("SELECT VERSION()");
		}

		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void test() {}
	
	public void dbInsert(String string) {
		//TODO
	}

}

