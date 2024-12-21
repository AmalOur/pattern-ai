package ma.projet.patternai.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "langchain_pg_embedding")
public class LangchainEmbedding {
    @Id
    private UUID uuid;

    @Column(name = "collection_id")
    private UUID collectionId;

    @Column(columnDefinition = "vector(384)")
    private double[] embedding;

    @Column
    private String document;

    @Column(columnDefinition = "jsonb")
    private String cmetadata;

    @Column(nullable = false)
    private String username;

    // Getters and Setters

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(UUID collectionId) {
        this.collectionId = collectionId;
    }

    public double[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(double[] embedding) {
        this.embedding = embedding;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
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
}