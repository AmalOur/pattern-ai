package ma.projet.patternai.repo;

import ma.projet.patternai.entities.Discussion;
import ma.projet.patternai.entities.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, UUID> {

    /**
     * Find all discussions in a space, ordered by creation time descending
     */
    List<Discussion> findBySpaceOrderByCreatedAtDesc(Space space);

    /**
     * Find discussions in a space of a specific type, ordered by creation time descending
     */
    List<Discussion> findBySpaceAndMessageTypeOrderByCreatedAtDesc(Space space, String messageType);

    /**
     * Delete all discussions in a space
     */
    void deleteBySpace(Space space);

    /**
     * Find all discussions in a space within a specific time range
     */
    List<Discussion> findBySpaceAndCreatedAtBetweenOrderByCreatedAtDesc(
            Space space,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * Count discussions in a space
     */
    long countBySpace(Space space);

    /**
     * Count discussions in a space by message type
     */
    long countBySpaceAndMessageType(Space space, String messageType);

    /**
     * Find latest discussion in a space
     */
    Optional<Discussion> findFirstBySpaceOrderByCreatedAtDesc(Space space);

    /**
     * Find latest discussion of a specific type in a space
     */
    Optional<Discussion> findFirstBySpaceAndMessageTypeOrderByCreatedAtDesc(
            Space space,
            String messageType
    );
}