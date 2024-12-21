package ma.projet.patternai.repo;

import ma.projet.patternai.entities.LangchainCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LangchainCollectionRepository extends JpaRepository<LangchainCollection, UUID> {
    List<LangchainCollection> findBySpaceIdAndUsernameOrderByCreatedAtDesc(UUID spaceId, String username);
    Optional<LangchainCollection> findByUuidAndSpaceIdAndUsername(UUID uuid, UUID spaceId, String username);
    void deleteBySpaceIdAndUsername(UUID spaceId, String username);
}