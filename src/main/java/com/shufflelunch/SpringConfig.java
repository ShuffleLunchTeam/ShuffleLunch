package com.shufflelunch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.shufflelunch.config.ConfigService;
import com.shufflelunch.config.Keys;

@PropertySource("classpath:application.properties")
@Configuration
public class SpringConfig {

    @Autowired
    ConfigService configService;

    @Bean
    public FirebaseAuth firebaseAuth() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(getClass().getResourceAsStream(configService.get(Keys.FIREBASE_KEY_PATH)))
                .setDatabaseUrl(configService.get(Keys.FIREBASE_DB_URL))
                .build();
        FirebaseApp.initializeApp(options);
        return FirebaseAuth.getInstance();
    }
}
