package ma.projet.patternai.repo;

import ma.projet.patternai.entities.Space;
import ma.projet.patternai.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpaceRepository extends JpaRepository<Space, UUID> {
    List<Space> findByUserOrderByCreatedAtDesc(User user);
    Optional<Space> findByIdAndUser_Email(UUID spaceId, String userEmail);
}