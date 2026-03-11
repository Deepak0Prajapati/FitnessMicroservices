package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityAiService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String prompt=createPromptForActivity(activity);
        String aiResponse=geminiService.getRecommendation(prompt);
        log.info("RESPONSE FROM AI: {}",aiResponse);
        return processAIResponse(activity,aiResponse);
    }

    private Recommendation processAIResponse(Activity activity, String aiResponse) {
        try{
            ObjectMapper objectMapper=new ObjectMapper();
            JsonNode rootNode=objectMapper.readTree(aiResponse);
            JsonNode textNode=rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .get("parts")
                    .get(0)
                    .path("text");
            String jsonContent=textNode.asString()
                    .replaceAll("\\n ```","")
                    .trim();
            log.info("Cleaner RESPONSE FROM AI: {}",jsonContent);

            JsonNode analysisJson=objectMapper.readTree(jsonContent);
            JsonNode analysisNode=analysisJson.path("analysis");
            StringBuilder fullAnalysis=new StringBuilder();
            addAnalysisSection(fullAnalysis,analysisNode,"overall","Overall :");
            addAnalysisSection(fullAnalysis,analysisNode,"pace","Pace :");
            addAnalysisSection(fullAnalysis,analysisNode,"heartRate","Heart Rate :");
            addAnalysisSection(fullAnalysis,analysisNode,"caloriesBurned","Calories :");

            List<String> improvements=extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions=extractSuggestions(analysisJson.path("suggestions"));
            List<String> safetyGuideLines=extractSafetyGuideLines(analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .type(activity.getActivityType().toString())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .safety(safetyGuideLines)
                    .suggestions(suggestions)
                    .createdAt(LocalDateTime.now())
                    .build();



        }catch (Exception e){
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getActivityType().toString())
                .recommendation("Unable to generate detailed analysis!")
                .improvements(Collections.singletonList("Continue with your current routine!"))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your Body"
                ))
                .suggestions(Collections.singletonList("Consider consulting a fitness consultant"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafetyGuideLines(JsonNode safetyNode) {
        List<String> safetyGuideLines=new ArrayList<>();
        if(safetyNode.isArray()){
            safetyNode.forEach(safety->{
                safetyGuideLines.add(safety.asString());
            });
        }
        return safetyGuideLines.isEmpty()?
                Collections.singletonList("Follow general Safety Guidelines!"):
                safetyGuideLines;
    }

    private List<String> extractSuggestions(JsonNode suggestionNode) {
        List<String> suggestions=new ArrayList<>();
        if (suggestionNode.isArray()){
            suggestionNode.forEach(suggestion->{
                String workout=suggestion.path("workout").asString();
                String details=suggestion.path("description").asString();
                suggestions.add(String.format("%s : %s",workout, details));
            });
        }
        return suggestions.isEmpty()?
                Collections.singletonList("No such Workout Suggestion!"):
                suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementNode) {
        List<String> improvements=new ArrayList<>();
        if (improvementNode.isArray()){
            improvementNode.forEach(improvement->{
                String area=improvement.path("area").asString();
                String recommendation=improvement.path("recommendation").asString();
                improvements.add(String.format("%s : %s",area,recommendation));
            });
        }
        return improvements.isEmpty()?
                Collections.singletonList("No such Improvement found"):
                improvements;

    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.isMissingNode()){
            fullAnalysis.append(prefix)
                    .append(analysisNode.get(key).asString())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ]
        }

        Analyze this activity:
        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s
        
        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
        Ensure the response follows the EXACT JSON format shown above.
        """,
                activity.getActivityType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }


}
