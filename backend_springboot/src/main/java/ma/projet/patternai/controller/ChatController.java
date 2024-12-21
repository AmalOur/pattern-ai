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

//    private String buildPromptWithContext(UUID spaceId, String userQuery, String userEmail) {
//        StringBuilder promptBuilder = new StringBuilder();
//        promptBuilder.append("You are a design pattern expert. ")
//                .append("Provide clear, concise, and contextually appropriate responses. ");
//
//        // Add previous discussions
//        List<String> previousDiscussions = discussionService.getPreviousDiscussions(spaceId, userEmail);
//        if (!previousDiscussions.isEmpty()) {
//            promptBuilder.append("\n\nPrevious conversation:\n");
//            previousDiscussions.stream()
//                    .limit(5)
//                    .forEach(msg -> promptBuilder.append(msg).append("\n"));
//        }
//
//        // Get code from langchain database
//        List<Map<String, Object>> processedCodes = langchainService.getSpaceCodeContent(spaceId, userEmail);
//        if (!processedCodes.isEmpty()) {
//            promptBuilder.append("\nAnalyzing code from the following repositories:\n");
//            for (Map<String, Object> code : processedCodes) {
//                String codeContent = (String) code.get("code_content");
//                if (codeContent != null && !codeContent.trim().isEmpty()) {
//                    promptBuilder.append("\nRepository: ")
//                            .append(code.get("repo_url"))
//                            .append("\n```\n")
//                            .append(codeContent)
//                            .append("\n```\n");
//                }
//            }
//        }
//
//        // Add code from current query if present
//        String codeFromQuery = extractCodeFromQuery(userQuery);
//        if (codeFromQuery != null) {
//            promptBuilder.append("\nAdditional code provided for analysis:\n```\n")
//                    .append(codeFromQuery)
//                    .append("\n```\n");
//        }
//
//        // Add current query
//        promptBuilder.append("\nCurrent query: ").append(userQuery).append("\n\n");
//
//        // Add specific instructions
//        if (processedCodes.isEmpty() && codeFromQuery == null) {
//            // No code context, handle as general design pattern question
//            if (isGreeting(userQuery)) {
//                promptBuilder.append("Welcome the user and offer guidance about design patterns.");
//            } else if (containsPatternName(userQuery)) {
//                promptBuilder.append("Explain the mentioned design pattern with practical examples and use cases.");
//            } else {
//                promptBuilder.append("Provide a clear, focused response about design patterns, ")
//                        .append("addressing the specific question or topic.");
//            }
//        } else {
//            // Code context available
//            promptBuilder.append("Please analyze the provided code and:\n")
//                    .append("1. Identify existing design patterns if any\n")
//                    .append("2. Recommend appropriate design patterns that could improve the code\n")
//                    .append("3. Explain how each recommended pattern would benefit this specific code\n")
//                    .append("4. Provide specific examples of how to refactor the code using these patterns\n")
//                    .append("5. Point out any potential architectural improvements\n");
//        }
//
//        logger.info("Generated prompt with {} processed code repositories", processedCodes.size());
//        return promptBuilder.toString();
//    }

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

            // Add specific instructions for code analysis
            promptBuilder.append("\nPlease analyze the above code and:\n")
                    .append("1. Identify any design patterns currently in use\n")
                    .append("2. Suggest appropriate design patterns that could improve the code\n")
                    .append("3. Explain how each suggested pattern would benefit this specific code\n")
                    .append("4. Provide implementation guidance for the suggested patterns\n\n");
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

    private boolean isCodeAnalysisQuery(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("explain") ||
                lowerQuery.contains("analyze") ||
                lowerQuery.contains("recommend") ||
                lowerQuery.contains("suggest") ||
                lowerQuery.contains("improve") ||
                lowerQuery.contains("pattern") ||
                lowerQuery.contains("review") ||
                lowerQuery.contains("code");
    }

    private void addQuerySpecificInstructions(StringBuilder promptBuilder, String userQuery,
                                              String codeFromQuery, boolean hasProcessedCode) {
        if (codeFromQuery != null || hasProcessedCode) {
            promptBuilder.append("Analyze the code and provide specific design pattern recommendations. ")
                    .append("Focus on improving code structure and maintainability. ")
                    .append("Explain the benefits of each suggested pattern.");
        } else if (isGreeting(userQuery)) {
            promptBuilder.append("Welcome the user and offer guidance about design patterns.");
        } else if (containsPatternName(userQuery)) {
            promptBuilder.append("Explain the mentioned design pattern with practical examples and use cases.");
        } else {
            promptBuilder.append("Provide a clear, focused response about design patterns, ")
                    .append("addressing the specific question or topic.");
        }
    }

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

    private String extractCodeFromQuery(String query) {
        var matcher = CODE_BLOCK_PATTERN.matcher(query);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private boolean isGreeting(String query) {
        query = query.toLowerCase().trim();
        return query.matches(".*(hi|hello|hey|greetings|good morning|good afternoon|good evening).*")
                && query.length() < 20;
    }

    private boolean containsPatternName(String query) {
        query = query.toLowerCase();
        String[] patterns = {
                "singleton", "factory", "abstract factory", "builder", "prototype",
                "adapter", "bridge", "composite", "decorator", "facade",
                "flyweight", "proxy", "chain of responsibility", "command",
                "interpreter", "iterator", "mediator", "memento", "observer",
                "state", "strategy", "template method", "visitor"
        };

        for (String pattern : patterns) {
            if (query.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}