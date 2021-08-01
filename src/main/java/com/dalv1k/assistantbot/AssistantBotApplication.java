package com.dalv1k.assistantbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AssistantBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssistantBotApplication.class, args);
    }
}
