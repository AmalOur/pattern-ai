package ma.projet.patternai.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAIClient {
    private final String apiKey;
    private final String baseUrl;
    private String model;
    private final double temperature;
    private final ObjectMapper objectMapper;
    private final AIModelConfig aiModelConfig;

    public OpenAIClient(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.chat.options.model}") String model,
            @Value("${spring.ai.openai.chat.options.temperature}") double temperature,
            AIModelConfig aiModelConfig
    ) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.temperature = temperature;
        this.aiModelConfig = aiModelConfig;
        this.objectMapper = new ObjectMapper();
    }

    public void setModel(String modelId) {
        boolean validModel = aiModelConfig.getModels().stream()
                .anyMatch(m -> m.getId().equals(modelId));
        if (validModel) {
            this.model = modelId;
        } else {
            throw new IllegalArgumentException("Invalid model ID: " + modelId);
        }
    }

    public List<AIModelConfig.Model> getAvailableModels() {
        return aiModelConfig.getModels();
    }

    public String getCurrentModel() {
        return this.model;
    }

    public Map<String, Object> generateCompletion(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", temperature);
            requestBody.put("messages", Arrays.asList(
                    Map.of(
                            "role", "system",
                            "content", """
            You are a design pattern expert specialized in analyzing 
            and improving software architectural quality. Your role is to:
            
            1. Identify recurring issues in the code (high coupling, 
               duplication, lack of flexibility)
            2. Recommend appropriate design patterns with detailed 
               contextual explanations
            3. Provide practical and context-adapted implementation guidance
            4. Evaluate potential impact on code quality
            
            If the user greets you, welcome them and ask how you can 
            help with design patterns.
            """
                    ),
                    Map.of("role", "user", "content", prompt)
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions",
                    request,
                    String.class
            );

            return parseOpenAIResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error generating completion: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> parseOpenAIResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String content = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            Map<String, Object> response = new HashMap<>();
            response.put("full_response", content);
            response.put("model", model);
            response.put("usage", root.path("usage"));

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing OpenAI response: " + e.getMessage(), e);
        }
    }
}