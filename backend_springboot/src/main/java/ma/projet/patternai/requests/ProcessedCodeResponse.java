package ma.projet.patternai.requests;

import ma.projet.patternai.entities.DesignPatternRecommendation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProcessedCodeResponse {
    private UUID id;
    private String repositoryUrl;
    private String collectionName;
    private LocalDateTime processedAt;
    private List<DesignPatternRecommendation> recommendations;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public List<DesignPatternRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<DesignPatternRecommendation> recommendations) {
        this.recommendations = recommendations;
    }
}
