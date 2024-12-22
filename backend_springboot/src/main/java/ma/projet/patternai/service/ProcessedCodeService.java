package ma.projet.patternai.service;

import ma.projet.patternai.entities.LangchainCollection;
import ma.projet.patternai.entities.Space;
import ma.projet.patternai.repo.LangchainCollectionRepository;
import ma.projet.patternai.repo.SpaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ProcessedCodeService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessedCodeService.class);

    @Autowired
    private LangchainCollectionRepository langchainCollectionRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private RestTemplate restTemplate;

    public List<LangchainCollection> getSpaceProcessedCodes(UUID spaceId, String userEmail) {
        logger.debug("Fetching processed codes for space {} and user {}", spaceId, userEmail);

        // Verify space exists and user has access
        Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                .orElseThrow(() -> new RuntimeException("Space not found or unauthorized"));

        // Fetch collections sorted by creation date (newest first)
        List<LangchainCollection> collections = langchainCollectionRepository
                .findBySpaceIdAndUsernameOrderByCreatedAtDesc(spaceId.toString(), userEmail);

        logger.debug("Found {} processed code collections", collections.size());
        return collections;
    }

    @Transactional
    public void deleteProcessedCode(UUID spaceId, UUID collectionId, String userEmail) {
        logger.debug("Deleting processed code {} from space {}", collectionId, spaceId);

        // Verify space exists and user has access
        Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                .orElseThrow(() -> new RuntimeException("Space not found or unauthorized"));

        // Find and verify collection
        LangchainCollection collection = langchainCollectionRepository
                .findByUuidAndSpaceIdAndUsername(collectionId, spaceId.toString(), userEmail)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        try {
            // First delete from database
            langchainCollectionRepository.delete(collection);

            // Then try to delete from vector store
            try {
                deleteVectorStoreCollection(collectionId.toString(), userEmail);
            } catch (Exception e) {
                // Log the error but don't fail the operation
                logger.warn("Failed to delete from vector store: {}", e.getMessage());
            }

            logger.debug("Successfully deleted processed code collection");
        } catch (Exception e) {
            logger.error("Error deleting collection: ", e);
            throw new RuntimeException("Failed to delete collection", e);
        }
    }

    private void deleteVectorStoreCollection(String collectionId, String userEmail) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", userEmail);
        requestBody.put("collection_id", collectionId);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        String url = "http://localhost:8000/api/collections/" + URLEncoder.encode(collectionId, StandardCharsets.UTF_8);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );
        } catch (Exception e) {
            logger.error("Error deleting collection from vector store: ", e);
            throw new RuntimeException("Failed to delete collection from vector store", e);
        }
    }
}