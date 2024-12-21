package ma.projet.patternai.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "langchain_pg_collection")
public class LangchainCollection {
    @Id
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(name = "repo_url")
    private String repoUrl;

    @Column(name = "space_id")
    private UUID spaceId;

    @Column(columnDefinition = "jsonb")
    private String cmetadata;

    @Column(nullable = false)
    private String username;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters and Setters
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(UUID spaceId) {
        this.spaceId = spaceId;
    }

    public String getCmetadata() {
        return cmetadata;
    }

    public void setCmetadata(String cmetadata) {
        this.cmetadata = cmetadata;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}