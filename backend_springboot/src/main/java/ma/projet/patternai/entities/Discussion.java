package ma.projet.patternai.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "discussions")
@Data
public class Discussion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String message;
    private LocalDateTime createdAt;
    private String messageType;

    @ManyToOne
    @JoinColumn(name = "space_id")
    @JsonBackReference
    private Space space;
}