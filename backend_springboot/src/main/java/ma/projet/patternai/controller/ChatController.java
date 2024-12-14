package ma.projet.patternai.controller;

import ma.projet.patternai.config.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private OpenAIClient openAIClient;

    @PostMapping("/design-patterns")
    public ResponseEntity<?> chatAboutDesignPatterns(
            @RequestBody Map<String, String> request
    ) {
        try {
            String userQuery = request.get("query");
            if (userQuery == null || userQuery.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Query is required"));
            }

            // Enhance the prompt with more specific instructions
            String prompt = String.format(
                    "Please help me understand design patterns, specifically about: %s\n\n" +
                            "Please provide:\n" +
                            "1. A clear explanation of the pattern\n" +
                            "2. When to use it (use cases)\n" +
                            "3. Code example if relevant\n" +
                            "4. Common pitfalls to avoid\n" +
                            "5. Related patterns worth considering",
                    userQuery
            );

            Map<String, Object> response = openAIClient.generateCompletion(prompt);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error processing request",
                            "message", e.getMessage()
                    ));
        }
    }
}