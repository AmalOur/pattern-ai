package ma.projet.patternai.controller;

import ma.projet.patternai.entities.DesignPatternRecommendation;
import ma.projet.patternai.entities.LangchainCollection;
import ma.projet.patternai.entities.Space;
import ma.projet.patternai.requests.CodeAnalysisRequest;
import ma.projet.patternai.service.DesignPatternRecommendationService;
import ma.projet.patternai.service.ProcessedCodeService;
import ma.projet.patternai.service.SpaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/spaces")
public class SpaceController {
    private static final Logger logger = LoggerFactory.getLogger(SpaceController.class);

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private ProcessedCodeService processedCodeService;

    @Autowired
    private DesignPatternRecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<Space>> getUserSpaces(Authentication auth) {
        String userEmail = auth.getName();
        logger.debug("Getting spaces for user: {}", userEmail);
        return ResponseEntity.ok(spaceService.getUserSpaces(userEmail));
    }

    @PostMapping
    public ResponseEntity<Space> createSpace(@RequestBody Space space, Authentication auth) {
        String userEmail = auth.getName();
        logger.debug("Creating space for user: {}", userEmail);
        return ResponseEntity.ok(spaceService.createSpace(space, userEmail));
    }

    @DeleteMapping("/{spaceId}")
    public ResponseEntity<?> deleteSpace(@PathVariable UUID spaceId, Authentication auth) {
        String userEmail = auth.getName();
        logger.debug("Deleting space {} for user: {}", spaceId, userEmail);
        spaceService.deleteSpace(spaceId, userEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{spaceId}/collections")
    public ResponseEntity<List<LangchainCollection>> getSpaceProcessedCodes(
            @PathVariable UUID spaceId,
            Authentication auth
    ) {
        try {
            String userEmail = auth.getName();
            logger.debug("Fetching processed code for space: {} and user: {}", spaceId, userEmail);
            List<LangchainCollection> processedCodes = processedCodeService.getSpaceProcessedCodes(spaceId, userEmail);
            return ResponseEntity.ok(processedCodes);
        } catch (Exception e) {
            logger.error("Error fetching processed code: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{spaceId}/collections/{collectionId}")
    public ResponseEntity<?> deleteProcessedCode(
            @PathVariable UUID spaceId,
            @PathVariable UUID collectionId,
            Authentication auth
    ) {
        try {
            String userEmail = auth.getName();
            logger.debug("Deleting processed code {} from space {} for user: {}",
                    collectionId, spaceId, userEmail);
            processedCodeService.deleteProcessedCode(spaceId, collectionId, userEmail);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting processed code: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{spaceId}/analyze")
    public ResponseEntity<?> analyzeCode(
            @PathVariable UUID spaceId,
            @RequestBody CodeAnalysisRequest request,
            Authentication auth
    ) {
        try {
            List<DesignPatternRecommendation> recommendations =
                    recommendationService.analyzeAndStoreCode(spaceId, request, auth.getName());
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error analyzing code: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}