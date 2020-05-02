package argssearch.entity;

public class Aspect {

    private String name;
    private double weight;
    private double normalizedWeight;
    private int rank;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getNormalizedWeight() {
        return normalizedWeight;
    }

    public void setNormalizedWeight(double normalizedWeight) {
        this.normalizedWeight = normalizedWeight;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
