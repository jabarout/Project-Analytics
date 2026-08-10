package com.projectanalytics.infrastructure.openproject;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenProjectProperties.class)
public class OpenProjectConfiguration {

    @Bean
    public RestClient.Builder openProjectRestClientBuilder() {
        return RestClient.builder();
    }
}
