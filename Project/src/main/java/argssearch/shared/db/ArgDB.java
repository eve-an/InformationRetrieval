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
    private final String USERNAME = "irargdb";
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
    public void dropAll() {
        try (PreparedStatement ps = prepareStatement(String.format("DROP owned by %s", USERNAME))) {
            ps.execute();
            conn.createStatement().execute("CREATE SCHEMA public");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        logger.info("Dropped everything.");
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
                "trigger_bootstrap.sql"
        ).forEachOrdered(this::executeSqlFile);

        logger.info("Created a new Schema.");
    }

    public void executeSqlFile(String name) {
        final String path = getClass().getResource("/database/" + name).getPath();
        final String postgresCmd = String.format("psql -U %s -d %s -f %s", USERNAME, DB_NAME, path);

        final ProcessBuilder pb = new ProcessBuilder();
        final String os = System.getProperty("os.name");
        try {
            if (os.equalsIgnoreCase("linux")) {
                pb.command("bash", "-c", postgresCmd);
            } else if (os.equalsIgnoreCase("windows")) {
                pb.command("cmd.exe", "/c", postgresCmd);   // TODO: Test
            }
            Process p = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String err;
            while ((err = br.readLine()) != null) {
                if (err.contains("drop cascades")) {
                    continue;
                }
                logger.error(err);
            }

            if (p.waitFor() != 0) {
                throw new IOException();
            }
        } catch (IOException | InterruptedException e) {
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

