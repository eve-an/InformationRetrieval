package argssearch.entity;

import java.util.LinkedHashSet;

public class ArgumentList {

    // Its just faster than an ArrayList
    // See: https://gist.github.com/psayre23/c30a821239f4818b0709
    private LinkedHashSet<Argument> arguments;

    public LinkedHashSet<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(LinkedHashSet<Argument> arguments) {
        this.arguments = arguments;
    }
}
