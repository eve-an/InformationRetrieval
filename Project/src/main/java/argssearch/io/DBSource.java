package argssearch.io;


import java.util.Objects;

class Source {

    private final String domain;

    public Source(JsonArgument argument) {
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
        Source source = (Source) o;
        return domain.equals(source.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain);
    }
}
