package argssearch.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Premise {

    private String text;
    private String stance;
    private List<Annotation> annotations;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Stance getStance() {
        return stance.equalsIgnoreCase("PRO") ? Stance.PRO : Stance.CON;
    }

    public void setStance(String stance) {
        this.stance = stance;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    @Override
    public String toString() {
        return "Premise{" +
                "text='" + text + '\'' +
                ", stance='" + stance + '\'' +
                '}';
    }
}
