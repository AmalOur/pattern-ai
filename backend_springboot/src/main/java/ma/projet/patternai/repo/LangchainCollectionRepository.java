package ma.projet.patternai.repo;

import ma.projet.patternai.entities.LangchainCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LangchainCollectionRepository extends JpaRepository<LangchainCollection, UUID> {
    @Query(value = "SELECT * FROM langchain_pg_collection " +
            "WHERE username = :username " +
            "AND cmetadata->>'space_id' = :spaceId " +
            "ORDER BY created_at DESC", nativeQuery = true)
    List<LangchainCollection> findBySpaceIdAndUsernameOrderByCreatedAtDesc(String spaceId, String username);

    @Query(value = "SELECT * FROM langchain_pg_collection " +
            "WHERE uuid = :uuid " +
            "AND cmetadata->>'space_id' = :spaceId " +
            "AND username = :username", nativeQuery = true)
    Optional<LangchainCollection> findByUuidAndSpaceIdAndUsername(UUID uuid, String spaceId, String username);
}