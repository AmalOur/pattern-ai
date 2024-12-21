package ma.projet.patternai.service;

import ma.projet.patternai.entities.DesignPatternRecommendation;
import ma.projet.patternai.requests.CodeAnalysisRequest;
import ma.projet.patternai.config.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DesignPatternRecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(DesignPatternRecommendationService.class);

    @Value("${app.python-service.url:http://localhost:8000}")
    private String pythonServiceUrl;

    @Autowired
    private OpenAIClient openAiClient;

    @Autowired
    private RestTemplate restTemplate;

    @Transactional
    public List<DesignPatternRecommendation> analyzeAndStoreCode(
            UUID spaceId,
            CodeAnalysisRequest request,
            String userEmail
    ) {
        try {
            logger.debug("Starting code analysis for space: {} and repo: {}",
                    spaceId, request.getRepositoryUrl());

            // First, store code in vector store through Python service
            String collectionName = storeCodeInVectorStore(spaceId, request, userEmail);
            logger.debug("Code stored successfully in collection: {}", collectionName);

            // Generate recommendations
            String prompt = generateAnalysisPrompt(request.getCodeContent());
            Map<String, Object> aiResponse = openAiClient.generateCompletion(prompt);
            String fullResponse = (String) aiResponse.get("full_response");

            return parseRecommendations(fullResponse);
        } catch (Exception e) {
            logger.error("Error analyzing code: ", e);
            throw new RuntimeException("Failed to analyze code: " + e.getMessage());
        }
    }

    private String storeCodeInVectorStore(
            UUID spaceId,
            CodeAnalysisRequest request,
            String userEmail
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String collectionName = request.getRepositoryUrl();

        // Create metadata correctly
        Map<String, String> metadata = new HashMap<>();
        metadata.put("space_id", spaceId.toString());
        metadata.put("created_at", LocalDateTime.now().toString());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("repository_url", request.getRepositoryUrl());
        requestBody.put("repo_url", request.getRepositoryUrl());  // Add this line
        requestBody.put("github_token", request.getGithubToken());
        requestBody.put("username", userEmail);
        requestBody.put("space_id", spaceId.toString());
        requestBody.put("collection_name", collectionName);
        requestBody.put("cmetadata", metadata);  // Changed from metadata to cmetadata to match Python's expectation

        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);

        try {
            logger.debug("Sending request to Python service for repo: {} with metadata: {}",
                    request.getRepositoryUrl(), metadata);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonServiceUrl + "/api/embed_github_code",
                    httpRequest,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Failed response from Python service: {}", response.getBody());
                throw new RuntimeException("Failed to store code in vector store: " + response.getBody());
            }

            return collectionName;
        } catch (Exception e) {
            logger.error("Error storing code in vector store: {} - {}",
                    request.getRepositoryUrl(), e.getMessage());
            throw new RuntimeException("Failed to store code: " + e.getMessage());
        }
    }

    private String generateAnalysisPrompt(String codeContent) {
        return String.format("""
            Act as an expert software architect specializing in design patterns. 
            Analyze this code and recommend appropriate design patterns:
            
            ```
            %s
            ```
            
            Please provide your recommendations in this exact format:
            PatternName | Detailed explanation of how it improves this specific code | Confidence (0-1)
            
            Focus on:
            1. The patterns that would most benefit this code
            2. Code structure and relationships
            3. Maintainability and extensibility
            4. Current architecture and potential improvements
            5. Specific implementation details
            
            Limit to the 5 most relevant patterns. Be specific about how each pattern 
            would improve THIS code, not just general pattern descriptions.
            
            For each pattern, explain:
            - Why this pattern is appropriate for this specific code
            - How to implement it in this context
            - What specific problems it solves
            - Expected benefits after implementation
            """, codeContent);
    }

    private List<DesignPatternRecommendation> parseRecommendations(String aiResponse) {
        List<DesignPatternRecommendation> recommendations = new ArrayList<>();
        Pattern pattern = Pattern.compile("([^|]+)\\|([^|]+)\\|([0-9.]+)");

        Arrays.stream(aiResponse.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty() && line.contains("|"))
                .forEach(line -> {
                    try {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String patternName = matcher.group(1).trim();
                            String explanation = matcher.group(2).trim();
                            double confidence = Double.parseDouble(matcher.group(3).trim());

                            if (confidence >= 0 && confidence <= 1) {
                                recommendations.add(new DesignPatternRecommendation(
                                        patternName,
                                        explanation,
                                        confidence
                                ));
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse recommendation line: {} - {}",
                                line, e.getMessage());
                    }
                });

        return recommendations.stream()
                .sorted(Comparator.comparingDouble(DesignPatternRecommendation::getConfidenceScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDetailedAnalysis(String codeContent) {
        try {
            String detailedPrompt = String.format("""
                Perform a detailed design pattern analysis of this code:
                
                ```
                %s
                ```
                
                Provide your analysis in these sections:
                1. Identified Patterns (format: PatternName | Justification | ConfidenceScore)
                2. Code Structure Analysis:
                   - Architecture overview
                   - Component relationships
                   - Design strengths and weaknesses
                3. Improvement Recommendations:
                   - Pattern implementation suggestions
                   - Code restructuring recommendations
                   - Best practices to adopt
                4. Implementation Guidelines:
                   - Step-by-step refactoring suggestions
                   - Code examples where appropriate
                5. Related Patterns:
                   - Alternative patterns to consider
                   - Complementary patterns
                
                Focus on practical, implementation-specific recommendations that 
                directly relate to the provided code.
                """, codeContent);

            Map<String, Object> aiResponse = openAiClient.generateCompletion(detailedPrompt);

            Map<String, Object> analysis = new HashMap<>();
            analysis.put("recommendations", parseRecommendations((String) aiResponse.get("full_response")));
            analysis.put("detailed_explanation", aiResponse.get("full_response"));
            analysis.put("model_info", aiResponse.get("model"));
            analysis.put("token_usage", aiResponse.get("usage"));

            return analysis;
        } catch (Exception e) {
            logger.error("Error getting detailed analysis: ", e);
            throw new RuntimeException("Failed to generate detailed analysis: " + e.getMessage());
        }
    }
}