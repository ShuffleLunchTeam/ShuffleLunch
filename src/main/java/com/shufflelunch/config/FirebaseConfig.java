package com.shufflelunch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import lombok.Data;

/**
 * @author seisuke
 */
@Configuration
@ConfigurationProperties(prefix = "firebase")
@Data
public class FirebaseConfig {
    private String keyPath;
    private String dbUrl;

    @Bean
    public FirebaseDatabase firebaseDatabase() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(getClass().getResourceAsStream(keyPath))
                .setDatabaseUrl(dbUrl)
                .build();
        FirebaseApp.initializeApp(options);
        return FirebaseDatabase.getInstance();
    }

}
