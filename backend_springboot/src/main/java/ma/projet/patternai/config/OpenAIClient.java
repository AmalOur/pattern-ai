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
import java.util.Map;

@Component
public class OpenAIClient {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature}")
    private double temperature;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                            "content", "You are a design pattern expert. Provide clear, concise, and contextually appropriate responses. " +
                                    "If the user greets you, welcome them and ask how you can help with design patterns. " +
                                    "Keep responses focused on what was specifically asked."
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