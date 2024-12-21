package ma.projet.patternai.requests;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SpaceResponse {
    private UUID id;
    private String name;
    private LocalDateTime createdAt;
    private List<DiscussionResponse> discussions;
    private List<ProcessedCodeResponse> processedCodes;

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DiscussionResponse> getDiscussions() {
        return discussions;
    }

    public void setDiscussions(List<DiscussionResponse> discussions) {
        this.discussions = discussions;
    }

    public List<ProcessedCodeResponse> getProcessedCodes() {
        return processedCodes;
    }

    public void setProcessedCodes(List<ProcessedCodeResponse> processedCodes) {
        this.processedCodes = processedCodes;
    }
}