package ma.projet.patternai.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "spaces")
@Data
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"spaces", "password", "motdepasse"})
    private User user;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Discussion> discussions = new ArrayList<>();
}