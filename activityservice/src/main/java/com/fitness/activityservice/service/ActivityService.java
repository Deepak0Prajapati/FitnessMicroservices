package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    public ActivityResponse trackActivity(ActivityRequest request) {
        Boolean validUser = userValidationService.validateUser(request .getUserId());
        if (!validUser){
            throw new RuntimeException("Invalid User: "+request.getUserId());

        }

        Activity activity = activityRequestToActivity(request);

        Activity saved = activityRepository.save(activity);
        System.out.println("Saved ID: " + saved.getId());
        System.out.println("Total count: " + activityRepository.count());
        return activityRequestToActivityResponse(saved);

    }
    public ActivityResponse activityRequestToActivityResponse(Activity activity){
        return ActivityResponse.builder()
                .id(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getActivityType())
                .caloriesBurned(activity.getCaloriesBurned())
                .duration(activity.getDuration())
                .startTime(activity.getStartTime())
                .additionalMetrics(activity.getAdditionalMetrics())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }

    public Activity activityRequestToActivity(ActivityRequest request){
        return Activity.builder()
                .userId(request.getUserId())
                .activityType(request.getActivityType())
                .caloriesBurned(request.getCaloriesBurned())
                .duration(request.getDuration())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();
    }
}
