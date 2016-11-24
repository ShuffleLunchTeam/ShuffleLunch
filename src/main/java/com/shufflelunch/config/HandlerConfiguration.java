package com.shufflelunch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.shufflelunch.handler.SubscribeLunchHandler;

import com.linecorp.bot.client.LineSignatureValidator;

import lombok.NonNull;

@Configuration
public class HandlerConfiguration {

    @Bean
    public SubscribeLunchHandler subscribeLunchHandler() {
        return new SubscribeLunchHandler();
    }

    @Bean
    @Primary
    public LineSignatureValidator v() {
        return new LineSignatureValidator(new byte[1]) {
            @Override
            public boolean validateSignature(@NonNull byte[] content, @NonNull String headerSignature) {
                return true;
            }
        };
    }

    @Bean
    public ConfigService getConfigService() {
        return new ConfigService();
    }

}
