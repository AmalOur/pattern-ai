package ma.projet.patternai.controller;

import ma.projet.patternai.entities.CodeAnalysisRequest;
import ma.projet.patternai.entities.DesignPatternRecommendation;
import ma.projet.patternai.entities.Discussion;
import ma.projet.patternai.repo.DiscussionRepository;
import ma.projet.patternai.repo.UserRepository;
import ma.projet.patternai.service.DesignPatternRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
class DesignPatternController {
    @Autowired
    private DesignPatternRecommendationService recommendationService;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCode(
            @RequestBody CodeAnalysisRequest request
    ) {
        try {
            // Validate request
            if (request.getRepositoryUrl() == null || request.getRepositoryUrl().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Repository URL is required"));
            }
            if (request.getGithubToken() == null || request.getGithubToken().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "GitHub token is required"));
            }

            // Get user email from JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // First fetch the code
            Map<String, String> fetchBody = new HashMap<>();
            fetchBody.put("repository_url", request.getRepositoryUrl());
            fetchBody.put("github_token", request.getGithubToken());
            fetchBody.put("username", userEmail); // Add username to fetch request

            ResponseEntity<Map> codeResponse = restTemplate.postForEntity(
                    "http://localhost:8000/api/fetch_github_code",
                    fetchBody,
                    Map.class
            );

            if (!codeResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(codeResponse.getStatusCode())
                        .body(codeResponse.getBody());
            }

            // Then embed the code
            Map<String, String> embedBody = new HashMap<>();
            embedBody.put("repository_url", request.getRepositoryUrl());
            embedBody.put("collection_name", "repo_" + request.getRepositoryUrl().hashCode());
            embedBody.put("github_token", request.getGithubToken());
            embedBody.put("username", userEmail); // Add username to embed request

            ResponseEntity<Map> embedResponse = restTemplate.postForEntity(
                    "http://localhost:8000/api/embed_github_code",
                    embedBody,
                    Map.class
            );

            if (!embedResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(embedResponse.getStatusCode())
                        .body(embedResponse.getBody());
            }

            // Get design pattern recommendations
            List<DesignPatternRecommendation> recommendations =
                    recommendationService.recommendDesignPatterns(
                            codeResponse.getBody().get("code_content").toString()
                    );

            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error analyzing code: " + e.getMessage()));
        }
    }

    @PostMapping("/discussions")
    public ResponseEntity<Discussion> addDiscussion(
            @RequestBody Discussion discussion
    ) {
        // Get user email from JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        discussion.setDate(LocalDateTime.now());
        // You might want to set the user here based on the email
        discussion.setUser(userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found")));

        discussionRepository.save(discussion);
        return ResponseEntity.ok(discussion);
    }

    @GetMapping("/discussions")
    public ResponseEntity<List<Discussion>> getAllDiscussions() {
        List<Discussion> discussions = discussionRepository.findAll();
        return ResponseEntity.ok(discussions);
    }
}