package com.shufflelunch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.google.firebase.database.FirebaseDatabase;

/**
 * @author seisuke
 */
public class TestConfig {

    @Bean
    @Primary
    public FirebaseDatabase firebaseDatabase() {
        return null;
    }
}
