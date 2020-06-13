package argssearch.shared.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.stream.Stream;

public class ArgDB {

    private static final Logger logger = LoggerFactory.getLogger(ArgDB.class);
    private final Connection conn;
    private final String USERNAME = "postgres";
    private final String DB_NAME = "argdb";
    private final String DB_URL = "jdbc:postgresql://localhost:5432/" + DB_NAME;

    private ArgDB() {
        try {
            this.conn = DriverManager.getConnection(DB_URL, USERNAME, "");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static ArgDB getInstance() {
        if (InstanceHolder.argDBInstance == null) {
            InstanceHolder.argDBInstance = new ArgDB();
        }
        return InstanceHolder.argDBInstance;
    }

    /**
     * Drop everything owned by the given Username.
     */
    public void dropSchema(final String schema) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + schema +  " CASCADE");
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        logger.info("Dropped " + schema);
    }

    /**
     * Creates a new schema from sql files, given in the resources folder.
     */
    public void createSchema() {
        Stream.of(
                "source_bootstrap.sql",
                "discussion_bootstrap.sql",
                "premise_bootstrap.sql",
                "argument_bootstrap.sql",
                "token_bootstrap.sql",
                "index_bootstrap.sql",
                "trigger_bootstrap.sql",
                "functions_bootstrap.sql"
        ).forEachOrdered(file -> executeSqlFile("/database/" + file));

        logger.info("Created a new Schema.");
    }

    public void executeSqlFile(String relativePath) {
        final String path = getClass().getResource(relativePath).getPath();
        final ProcessBuilder pb = new ProcessBuilder("psql", "-U", USERNAME, "-d", DB_NAME, "-f", path);

        try {
            Process p = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String err;
            while ((err = br.readLine()) != null) {
                if (err.contains("drop cascades")) {
                    continue;
                }
                logger.error(err);
            }

        } catch (IOException e) {
            logger.error("Error while executing {}", path, e);
        }
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

	public static boolean isException(SQLException sqle) {
		return false;
	}

    public ResultSet query(String queryText) {
        try {
            return this.conn.createStatement().executeQuery(queryText);
        } catch (SQLException sqlE) {
            // TODO log
        }
        return null;
    }

    public long getSequenceId(String sequenceName) {
        try (ResultSet rs = query(String.format("SELECT last_value FROM %s", sequenceName))) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException throwables) {
            logger.warn("Could not retrieve current value of sequence '{}' - SQL-Code: {}", sequenceName,
                    throwables.getErrorCode(), throwables);
        }

        return -1;
    }

    public Array createArrayOf(String type, Object[] array) {
        try {
            return this.conn.createArrayOf(type, array);
        } catch (SQLException sqlE) {
            if (isException(sqlE)) {
                sqlE.printStackTrace();
            }
        }
        return null;
    }

    public long getRowCount(String table) {
        try (ResultSet rs = query(String.format("SELECT COUNT(*) FROM %s", table))) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return -1;
    }

    private static class InstanceHolder {
        private static ArgDB argDBInstance;
    }
}

