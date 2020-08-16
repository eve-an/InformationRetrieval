package argssearch.shared.db;

import argssearch.shared.util.FileHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLRunner {

    final static Pattern sqlFunction = Pattern.compile("(?i)PROCEDURE|FUNCTION|(?i)LANGUAGE\\s*(?i)plpgsql;");

    public void run(final String relativeSQLPath) throws IOException, SQLException {

        String sql = new FileHandler().getResourceAsString(relativeSQLPath);
        Matcher m = sqlFunction.matcher(sql);
        // Procedures and functions have another syntax than the rest of the sql statements
        if (m.find()) {
            run(sql, "(?<=(?i)LANGUAGE (?i)plpgsql;)");
        } else {
            run(sql, "(?<=;)");
        }

    }

    private void run(final String sql, final String splitRegex) throws SQLException {
        for (String statement : sql.split(splitRegex)) {
            if (!statement.isBlank()) {
                final String withoutComments = statement.replaceAll("--.*", "");
                ArgDB.getInstance().executeStatement(withoutComments);
            }
        }
    }
}
