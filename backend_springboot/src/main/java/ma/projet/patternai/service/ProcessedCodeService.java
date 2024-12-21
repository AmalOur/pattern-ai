package ma.projet.patternai.service;

import ma.projet.patternai.entities.LangchainCollection;
import ma.projet.patternai.entities.Space;
import ma.projet.patternai.repo.LangchainCollectionRepository;
import ma.projet.patternai.repo.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.*;

import java.util.*;

@Service
public class ProcessedCodeService {
    @Autowired
    private LangchainCollectionRepository langchainCollectionRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private RestTemplate restTemplate;

    public List<LangchainCollection> getSpaceProcessedCodes(UUID spaceId, String userEmail) {
        Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                .orElseThrow(() -> new RuntimeException("Space not found or unauthorized"));

        return langchainCollectionRepository.findBySpaceIdAndUsernameOrderByCreatedAtDesc(space.getId(), userEmail);
    }

    @Transactional
    public void deleteProcessedCode(UUID spaceId, UUID collectionId, String userEmail) {
        Space space = spaceRepository.findByIdAndUser_Email(spaceId, userEmail)
                .orElseThrow(() -> new RuntimeException("Space not found or unauthorized"));

        LangchainCollection collection = langchainCollectionRepository
                .findByUuidAndSpaceIdAndUsername(collectionId, spaceId, userEmail)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        // Delete from vector store (this will cascade to embeddings)
        langchainCollectionRepository.delete(collection);

        // Notify Python service
        deleteVectorStoreCollection(collection.getName(), userEmail);
    }

    private void deleteVectorStoreCollection(String collectionName, String userEmail) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", userEmail);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        restTemplate.exchange(
                "http://localhost:8000/api/collections/" + collectionName,
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }
}