package ma.projet.patternai.controller;

import ma.projet.patternai.config.OpenAIClient;
import ma.projet.patternai.entities.Discussion;
import ma.projet.patternai.entities.Space;
import ma.projet.patternai.service.DiscussionService;
import ma.projet.patternai.service.LangchainService;
import ma.projet.patternai.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/spaces/{spaceId}/chat")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```([^`]*?)```", Pattern.DOTALL);

    @Autowired
    private OpenAIClient openAIClient;

    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private LangchainService langchainService;

    @GetMapping("/history")
    public ResponseEntity<List<Discussion>> getChatHistory(
            @PathVariable UUID spaceId,
            Authentication auth
    ) {
        try {
            logger.debug("Fetching chat history for space: {} and user: {}", spaceId, auth.getName());
            List<Discussion> discussions = discussionService.getSpaceDiscussions(spaceId, auth.getName());
            return ResponseEntity.ok(discussions);
        } catch (Exception e) {
            logger.error("Error getting chat history: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/design-patterns")
    public ResponseEntity<?> chatAboutDesignPatterns(
            @PathVariable UUID spaceId,
            @RequestBody Map<String, String> request,
            Authentication auth
    ) {
        try {
            String userQuery = request.get("query");
            if (userQuery == null || userQuery.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Query is required"));
            }

            logger.debug("Getting code content for space: {}", spaceId);
            List<Map<String, Object>> processedCodes = langchainService.getSpaceCodeContent(spaceId, auth.getName());
            logger.debug("Found {} code collections", processedCodes.size());

            for (Map<String, Object> code : processedCodes) {
                String codeContent = (String) code.get("code_content");
                logger.debug("Code from repo {}: {}", code.get("repo_url"),
                        codeContent != null ? codeContent.substring(0, Math.min(100, codeContent.length())) + "..." : "null");
            }

            // Save user's message
            Discussion userDiscussion = discussionService.saveDiscussion(
                    spaceId,
                    "User: " + userQuery,
                    auth.getName(),
                    "CHAT"
            );

            // Build prompt with context from langchain and previous discussions
            String prompt = buildPromptWithContext(spaceId, userQuery, auth.getName());
            logger.debug("Generated prompt: {}", prompt);

            // Get AI response
            Map<String, Object> aiResponse = openAIClient.generateCompletion(prompt);
            String modelResponse = (String) aiResponse.get("full_response");

            // Save AI's response
            Discussion modelDiscussion = discussionService.saveDiscussion(
                    spaceId,
                    "Assistant: " + modelResponse,
                    auth.getName(),
                    "CHAT"
            );

            Map<String, Object> response = new HashMap<>(aiResponse);
            response.put("userMessage", userDiscussion);
            response.put("assistantMessage", modelDiscussion);
            response.put("codeFound", !processedCodes.isEmpty());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in chat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private String buildPromptWithContext(UUID spaceId, String userQuery, String userEmail) {
        StringBuilder promptBuilder = new StringBuilder();
        logger.debug("Building prompt for space {} and user {}", spaceId, userEmail);

        // Get code content first
        List<Map<String, Object>> processedCodes = langchainService.getSpaceCodeContent(spaceId, userEmail);
        boolean hasValidCode = false;

        if (!processedCodes.isEmpty()) {
            promptBuilder.append("You are analyzing a codebase with the following content:\n\n");

            for (Map<String, Object> codeFile : processedCodes) {
                String codeContent = (String) codeFile.get("code_content");
                String repoUrl = (String) codeFile.get("repo_url");

                if (codeContent != null && !codeContent.trim().isEmpty()) {
                    hasValidCode = true;
                    promptBuilder.append("Repository: ").append(repoUrl).append("\n")
                            .append("Code Content:\n```\n")
                            .append(codeContent.trim())
                            .append("\n```\n\n");

                    logger.debug("Added code from repo {} to prompt", repoUrl);
                }
            }

            // Enhanced analysis instructions
            promptBuilder.append("\nAnalyze the code structure and provide a detailed report in the following format:\n\n")
                    .append("1. CODE QUALITY METRICS:\n")
                    .append("   - Coupling level (High/Medium/Low)\n")
                    .append("   - Code duplication assessment\n")
                    .append("   - Class cohesion analysis\n")
                    .append("   - Dependency management evaluation\n\n")
                    .append("2. DESIGN PATTERNS:\n")
                    .append("   - Currently implemented patterns\n")
                    .append("   - Recommended patterns with justification\n")
                    .append("   - Implementation priority (High/Medium/Low)\n\n")
                    .append("3. ARCHITECTURAL RECOMMENDATIONS:\n")
                    .append("   - Current architecture overview\n")
                    .append("   - Suggested improvements\n")
                    .append("   - Refactoring priorities\n\n");
        }

        // Add previous discussions for context
        List<String> previousDiscussions = discussionService.getPreviousDiscussions(spaceId, userEmail);
        if (!previousDiscussions.isEmpty()) {
            promptBuilder.append("\nPrevious conversation context:\n");
            previousDiscussions.stream()
                    .limit(5)
                    .forEach(msg -> promptBuilder.append(msg).append("\n"));
        }

        // Add current query
        promptBuilder.append("\nCurrent query: ").append(userQuery);

        String finalPrompt = promptBuilder.toString();
        logger.debug("Generated prompt with code: {}, length: {}", hasValidCode, finalPrompt.length());
        return finalPrompt;
    }
}