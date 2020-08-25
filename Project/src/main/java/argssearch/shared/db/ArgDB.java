package argssearch.shared.db;

import argssearch.shared.exceptions.NoSQLResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class ArgDB {

    private static final Logger logger = LoggerFactory.getLogger(ArgDB.class);
    private final Connection conn;
    private final Properties props;

    // TODO make private
    public ArgDB() {
        try {
            // Register Postgres Driver
            Class.forName("org.postgresql.Driver");

            // Choose the right properties file
            if (getHostname().equals("tira-ubuntu")) {
                props = loadProperties("/database/db_montalet.properties");
            } else {
                props = loadProperties("/database/db_dev.properties");
            }

            this.conn = DriverManager.getConnection(props.getProperty("url"), props);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Could not get hostname for choosing the right properties file.", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not read database properties.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Check if Postgres JDBC Driver is installed.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to database.", e);
        }
    }

    private String getHostname() throws UnknownHostException {
        InetAddress addr;
        addr = InetAddress.getLocalHost();
        return addr.getHostName();
    }

    private Properties loadProperties(final String relativePath) throws IOException {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream(relativePath));
        return props;
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
        ).forEachOrdered(file -> executeSqlFile("/database/scripts/" + file));

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

    public void executeNativeSql(final String sql) throws IOException {
        List<String> cmds = List.of(
                "psql", "-U", props.getProperty("user", "postgres"),
                "-d", props.getProperty("name", ""),
                "-c", sql
        );
        final ProcessBuilder pb = new ProcessBuilder(cmds);
        Process p = pb.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String output;
        while ((output = br.readLine()) != null) {
            logger.info(output);
        }

    }

    public static boolean isException(SQLException sqle) {
        return false;
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
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
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
        } catch (IOException ex) {
            ex.printStackTrace();   // TODO handle
        }
    }

    public int getIndexOfTerm(String term) throws SQLException {
        PreparedStatement ps = prepareStatement("SELECT tid FROM token WHERE token = ?");
        ps.setString(1, term);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int index = rs.getInt(1);
                rs.close();
                return index;
            } else {
                rs.close();
                throw new NoSQLResultException("Did not found an index for '" + term + "'");
            }
        } finally {
            ps.close();
        }
    }

    public Statement getStatement() {
        try {
            return conn.createStatement();
        } catch (SQLException throwables) {
            throw new RuntimeException("Could not create Statement.");
        }
    }

    public void executeStatement(final String statement) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute(statement);
        stmt.close();
    }

    public void close() {
        try {
            if (!this.conn.isClosed())
                this.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class InstanceHolder {
        private static ArgDB argDBInstance;
    }
}

