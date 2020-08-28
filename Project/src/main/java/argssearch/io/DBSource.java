package argssearch.io;


import java.util.Objects;

class DBSource {

    private final String domain;

    public DBSource(JsonArgument argument) {
        domain = argument.getContext().getSourceDomain();
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DBSource DBSource = (DBSource) o;
        return domain.equals(DBSource.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain);
    }
}
