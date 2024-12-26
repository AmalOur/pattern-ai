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
}