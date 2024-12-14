package ma.projet.patternai.entities;

public class DesignPatternRecommendation {
    private String patternName;
    private String description;
    private double confidenceScore;

    public DesignPatternRecommendation(String patternName, String description, double confidenceScore) {
        this.patternName = patternName;
        this.description = description;
        this.confidenceScore = confidenceScore;
    }

    // Add getters and setters
    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}