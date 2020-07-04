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
    private Connection conn;
    private final String USERNAME = "irargdb";
    private final String DB_NAME = "argdb";
    private final String DB_URL = String.format("jdbc:postgresql://localhost:5432/%s?user=%s", DB_NAME, USERNAME);


    public void connectToDB(final String url) {
        try {
            // Register Postgres Driver
            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void connectToDB() {
        try {
            // Register Postgres Driver
            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException | ClassNotFoundException e) {
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
     * Drop given Schema
     */
    public void dropSchema(final String schema) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        logger.info("Dropped " + schema);
    }

    /**
     * Truncates table
     *
     * @param table table name
     */
    public void truncateTable(final String table) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE Table " + table);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        logger.info("Truncated Table " + table);
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
                "aggregate_bootstrap.sql",
                "functions_bootstrap.sql",
                "type_bootstrap.sql",                                                                                             
                "vectorspace.sql",
                "view_bootstrap.sql"
        ).forEachOrdered(file -> executeSqlFile("/database/" + file));

        logger.info("Created a new Schema.");
    }

    public void executeSqlFile(String relativePath) {
        String path = getClass().getResource(relativePath).getPath();
        if (System.getProperty("os.name").toLowerCase().startsWith("windows") && path.startsWith("/")) {
            path = path.substring(1);
        }
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

    public void executeNativeSql(final String sql) throws SQLException {

        final ProcessBuilder pb = new ProcessBuilder("psql", "-U", USERNAME, "-d", DB_NAME, "-c", sql);

        try {
            Process p = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String output;
            while ((output = br.readLine()) != null) {
                logger.info(output);
            }

        } catch (IOException e) {
            logger.error("Error while executing {}", sql, e);
        }
    }

    public CallableStatement prepareCall(final String sql) {
        try {
            return this.conn.prepareCall(sql);
        } catch (SQLException sqlE) {
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

    public int getRowCount(String table) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return -1;
    }

    public void clearTable(final String tableName) {
        try {
           executeNativeSql(String.format("DELETE FROM %s", tableName));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int getIndexOfTerm(String term) {

        try {
            PreparedStatement ps = ArgDB.getInstance().getConn().prepareStatement("SELECT tid FROM token WHERE token = ?");

            ps.setString(1, term);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 0;
    }

    public Statement getStatement() {
        try {
            return conn.createStatement();
        } catch (SQLException throwables) {
            throw new RuntimeException("Could not create Statement.");
        }
    }


    public Connection getConn() {
        return conn;
    }

    private static class InstanceHolder {
        private static ArgDB argDBInstance;
    }
}

