package argssearch.shared.db;

import argssearch.shared.exceptions.NoSQLResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.stream.Stream;

public class ArgDB {

    private static final Logger logger = LoggerFactory.getLogger(ArgDB.class);
    private Connection conn;
    private static final String USERNAME = "postgres";
    private static final String DB_NAME = "argdb";
    // Append &password=your_password when you have a password for your db
    public static String DB_URL = String.format("jdbc:postgresql://localhost:5432/%s?user=%s", DB_NAME, USERNAME);


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
            throw new RuntimeException(throwables);
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
                "aggregate_bootstrap.sql",
                "functions_bootstrap.sql",
                "procedure_bootstrap.sql",
                "view_bootstrap.sql"
        ).forEachOrdered(file -> executeSqlFile("/database/" + file));

        logger.info("Created a new Schema.");
    }

    public void executeSqlFile(String relativePath) {
        try {
            new SQLRunner().run(relativePath);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
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

    public int getIndexOfTerm(String term) throws SQLException {

        PreparedStatement ps = ArgDB.getInstance().getConn().prepareStatement("SELECT tid FROM token WHERE token = ?");

        ps.setString(1, term);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        } else throw new NoSQLResultException("Did not found an index for '" + term + "'");

    }

    public Statement getStatement() {
        try {
            return conn.createStatement();
        } catch (SQLException throwables) {
            throw new RuntimeException("Could not create Statement.");
        }
    }

    public void executeStatement(final String statement) throws SQLException {
        getConn().createStatement().execute(statement);
    }

    public Connection getConn() {
        return conn;
    }

    private static class InstanceHolder {
        private static ArgDB argDBInstance;
    }
}

