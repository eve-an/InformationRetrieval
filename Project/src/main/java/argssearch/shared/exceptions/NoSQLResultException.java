package argssearch.shared.exceptions;

import java.sql.SQLException;

/**
 * Thrown when a query does not return a result.
 */
public class NoSQLResultException extends SQLException {

    public NoSQLResultException(String reason) {
        super(reason);
    }
}
