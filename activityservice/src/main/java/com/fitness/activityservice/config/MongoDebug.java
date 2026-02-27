package com.fitness.activityservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class MongoDebug {

    @Value("${spring.data.mongodb.database:NOT_DEFINED}")
    private String database;

    @Value("${spring.data.mongodb.uri:NOT_DEFINED}")
    private String uri;
    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void printProps() {
        System.out.println("==== ACTUAL MONGO DATABASE ====");
        System.out.println("DB NAME: " + mongoTemplate.getDb().getName());
        System.out.println("===============================");
    }
}
