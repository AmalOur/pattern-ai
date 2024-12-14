package ma.projet.patternai.service;

import ma.projet.patternai.entities.DesignPatternRecommendation;
import ma.projet.patternai.config.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DesignPatternRecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(DesignPatternRecommendationService.class);

    @Autowired
    private OpenAIClient openAiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<DesignPatternRecommendation> recommendDesignPatterns(String codeContent) {
        try {
            // Generate the analysis prompt
            String prompt = generateAnalysisPrompt(codeContent);

            // Get recommendations from OpenAI
            Map<String, Object> aiResponse = openAiClient.generateCompletion(prompt);
            String fullResponse = (String) aiResponse.get("full_response");

            // Parse and validate recommendations
            return parseRecommendations(fullResponse);

        } catch (Exception e) {
            logger.error("Error getting design pattern recommendations: ", e);
            throw new RuntimeException("Failed to analyze code: " + e.getMessage());
        }
    }

    private String generateAnalysisPrompt(String codeContent) {
        return String.format("""
            Act as an expert software architect specializing in design patterns. Analyze the following code and recommend appropriate design patterns.

            CODE TO ANALYZE:
            ```
            %s
            ```

            INSTRUCTIONS:
            1. Identify design patterns that could improve this code
            2. For each pattern, explain specifically how it would benefit this code
            3. Provide a confidence score (0-1) for each recommendation
            4. Format each recommendation as: PatternName | Detailed explanation of benefit | ConfidenceScore
            5. Focus on the most relevant patterns (maximum 5)
            6. Consider:
               - Code structure and relationships
               - Potential maintenance issues
               - Flexibility and extensibility needs
               - Current anti-patterns or code smells

            EXAMPLE OUTPUT FORMAT:
            Strategy | Could improve the payment processing logic by making payment methods interchangeable | 0.9
            Observer | Would help decouple the notification system from core business logic | 0.85
            """, codeContent);
    }

    private List<DesignPatternRecommendation> parseRecommendations(String aiResponse) {
        List<DesignPatternRecommendation> recommendations = new ArrayList<>();

        // Split response into lines and process each recommendation
        String[] lines = aiResponse.split("\n");
        Pattern pattern = Pattern.compile("([^|]+)\\|([^|]+)\\|([0-9.]+)");

        for (String line : lines) {
            try {
                Matcher matcher = pattern.matcher(line.trim());
                if (matcher.find()) {
                    String patternName = matcher.group(1).trim();
                    String explanation = matcher.group(2).trim();
                    double confidence = Double.parseDouble(matcher.group(3).trim());

                    // Validate the confidence score
                    if (confidence >= 0 && confidence <= 1) {
                        recommendations.add(new DesignPatternRecommendation(
                                patternName,
                                explanation,
                                confidence
                        ));
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to parse recommendation line: " + line, e);
                // Continue processing other lines
            }
        }
        // Limit to top 5 recommendations
        return recommendations.stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDetailedAnalysis(String codeContent) {
        try {
            String detailedPrompt = generateDetailedAnalysisPrompt(codeContent);
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

    private String generateDetailedAnalysisPrompt(String codeContent) {
        return String.format("""
            Perform a detailed design pattern analysis of the following code.
            
            CODE:
            ```
            %s
            ```
            
            Please provide:
            1. Identified Design Patterns (format: PatternName | Justification | ConfidenceScore)
            2. Code Structure Analysis
            3. Potential Improvements
            4. Implementation Suggestions
            5. Related Patterns to Consider
            
            Be specific and provide practical examples where possible.
            """, codeContent);
    }
}