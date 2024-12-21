package ma.projet.patternai.controller;

import ma.projet.patternai.requests.CodeAnalysisRequest;
import ma.projet.patternai.entities.DesignPatternRecommendation;
import ma.projet.patternai.service.DesignPatternRecommendationService;
import ma.projet.patternai.service.LangchainService;
import ma.projet.patternai.service.ProcessedCodeService;
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
@RequestMapping("/api/spaces/{spaceId}")
public class DesignPatternController {
    private static final Logger logger = LoggerFactory.getLogger(DesignPatternController.class);

    @Autowired
    private DesignPatternRecommendationService recommendationService;

    @Autowired
    private LangchainService langchainService;

    @PostMapping("/analyze")
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

    @DeleteMapping("/collections/{collectionId}")
    public ResponseEntity<?> deleteProcessedCode(
            @PathVariable UUID spaceId,
            @PathVariable UUID collectionId,
            Authentication auth
    ) {
        try {
            langchainService.deleteCollection(spaceId, collectionId, auth.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting collection: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}