package ma.projet.patternai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.ai")
public class AIModelConfig {
    private List<Model> models;

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }

    public static class Model {
        private String id;
        private String name;
        private int contextLength;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getContextLength() {
            return contextLength;
        }

        public void setContextLength(int contextLength) {
            this.contextLength = contextLength;
        }
    }
}