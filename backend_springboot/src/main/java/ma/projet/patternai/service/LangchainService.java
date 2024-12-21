package ma.projet.patternai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class LangchainService {
    private static final Logger logger = LoggerFactory.getLogger(LangchainService.class);

    private final JdbcTemplate jdbcTemplate;

    public LangchainService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getSpaceCodeContent(UUID spaceId, String username) {
        String sql = """
            SELECT 
                c.uuid as collection_id,
                c.repo_url,
                c.created_at,
                e.document as code_content
            FROM langchain_pg_collection c
            INNER JOIN langchain_pg_embedding e ON e.collection_id = c.uuid
            WHERE c.cmetadata->>'space_id' = ?
            AND c.username = ?
            AND e.document IS NOT NULL
            ORDER BY c.created_at DESC;
            """;

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    sql,
                    spaceId.toString(), // Convert UUID to string since it's stored as string in JSONB
                    username
            );

            logger.debug("Found {} code segments for space {} and user {}",
                    results.size(), spaceId, username);

            // Log for debugging
            for (Map<String, Object> result : results) {
                String codeContent = (String) result.get("code_content");
                logger.debug("Code found for collection {}: {} characters",
                        result.get("collection_id"),
                        codeContent != null ? codeContent.length() : 0);
            }

            return results;
        } catch (Exception e) {
            logger.error("Error fetching code content for space {}: {}", spaceId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> searchSimilarCode(String codeQuery, UUID spaceId, String username) {
        String sql = """
            SELECT 
                c.uuid as collection_id,
                c.repo_url,
                e.document as code_content,
                c.created_at
            FROM langchain_pg_collection c
            INNER JOIN langchain_pg_embedding e ON e.collection_id = c.uuid
            WHERE c.space_id = ?
            AND c.username = ?
            AND e.document IS NOT NULL 
            AND length(trim(e.document)) > 0
            ORDER BY c.created_at DESC;
            """;

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, spaceId, username);
            logger.debug("Found {} code segments", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error searching similar code: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Transactional
    public void deleteCollection(UUID spaceId, UUID collectionId, String username) {
        String verifySql = """
            SELECT COUNT(1) 
            FROM langchain_pg_collection 
            WHERE uuid = ? 
            AND space_id = ? 
            AND username = ?
            """;

        try {
            int count = jdbcTemplate.queryForObject(verifySql, Integer.class, collectionId, spaceId, username);
            if (count == 0) {
                throw new RuntimeException("Collection not found or unauthorized access");
            }

            String deleteCollectionSql = "DELETE FROM langchain_pg_collection WHERE uuid = ?";
            jdbcTemplate.update(deleteCollectionSql, collectionId);
            logger.debug("Deleted collection {} for space {}", collectionId, spaceId);
        } catch (Exception e) {
            logger.error("Error deleting collection {}: {}", collectionId, e.getMessage());
            throw new RuntimeException("Failed to delete collection", e);
        }
    }
}