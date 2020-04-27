package argssearch.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Premise {

    private static final Logger logger = LogManager.getLogger(Premise.class);
    private String text;
    private String stance;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Stance getStance() {
        if (stance.equalsIgnoreCase("pro")) {
            return Stance.PRO;
        } else if (stance.equalsIgnoreCase("con")) {
            return Stance.CON;
        } else {
            logger.warn("Premise has no matching Stance.");
            return null;
        }
    }

    public void setStance(String stance) {
        this.stance = stance;
    }

    @Override
    public String toString() {
        return "Premise{" +
                "text='" + text + '\'' +
                ", stance='" + stance + '\'' +
                '}';
    }
}
