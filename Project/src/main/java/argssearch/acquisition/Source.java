package argssearch.acquisition;


import java.util.Objects;

class Source {

    private long sourceid;
    private String domain;

    public Source(JsonArgument argument) {
        domain = argument.getContext().getSourceDomain();
    }

    public long getSourceid() {
        return sourceid;
    }

    public void setSourceid(long sourceid) {
        this.sourceid = sourceid;
    }


    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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
