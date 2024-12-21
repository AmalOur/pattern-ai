package ma.projet.patternai.requests;

import ma.projet.patternai.entities.DesignPatternRecommendation;

import java.util.List;
import java.util.Map;

public class AnalysisResponse {
    private ProcessedCodeResponse processedCode;
    private List<DesignPatternRecommendation> recommendations;
    private Map<String, Object> additionalInfo;

    public ProcessedCodeResponse getProcessedCode() {
        return processedCode;
    }

    public void setProcessedCode(ProcessedCodeResponse processedCode) {
        this.processedCode = processedCode;
    }

    public List<DesignPatternRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<DesignPatternRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public Map<String,Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String,Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
