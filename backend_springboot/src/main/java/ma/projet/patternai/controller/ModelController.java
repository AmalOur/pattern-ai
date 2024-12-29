package ma.projet.patternai.controller;

import ma.projet.patternai.config.AIModelConfig;
import ma.projet.patternai.config.OpenAIClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/models")
public class ModelController {
    private final OpenAIClient openAIClient;

    public ModelController(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @GetMapping
    public ResponseEntity<List<AIModelConfig.Model>> getAvailableModels() {
        return ResponseEntity.ok(openAIClient.getAvailableModels());
    }

    @GetMapping("/current")
    public ResponseEntity<String> getCurrentModel() {
        return ResponseEntity.ok(openAIClient.getCurrentModel());
    }

    @PostMapping("/select")
    public ResponseEntity<?> selectModel(@RequestBody Map<String, String> request) {
        try {
            String modelId = request.get("modelId");
            openAIClient.setModel(modelId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}