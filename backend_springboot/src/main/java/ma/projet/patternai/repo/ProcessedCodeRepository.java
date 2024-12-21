package ma.projet.patternai.repo;

import ma.projet.patternai.entities.ProcessedCode;
import ma.projet.patternai.entities.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessedCodeRepository extends JpaRepository<ProcessedCode, UUID> {
    List<ProcessedCode> findBySpaceOrderByProcessedAtDesc(Space space);
    Optional<ProcessedCode> findByIdAndSpace(UUID id, Space space);
}