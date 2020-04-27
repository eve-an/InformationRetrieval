package argssearch.entity;

import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Argument {

    private static final Logger logger = LogManager.getLogger(Argument.class);
    private Premise[] premises; // In fact only one premise but its stored as an array
    private Context context;
    private String id;
    private String conclusion;

    /**
     * In the Json-File a premise is saved as an array. In fact it holds every time only one element
     * so we will returning that element.
     *
     * @return the argument's premise.
     */
    public Premise getPremise() {
        if (premises.length == 1) {
            return premises[0];
        } else {
            logger.warn("Found no premise.");
            return null;
        }
    }

    public void setPremises(Premise[] premises) {
        this.premises = premises;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "premises=" + Arrays.toString(premises) +
                ", context=" + context +
                ", id='" + id + '\'' +
                ", conclusion='" + conclusion + '\'' +
                '}';
    }
}
