package ma.projet.patternai.repo;

import ma.projet.patternai.entities.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiscussionRepository extends JpaRepository<Discussion, UUID> {}