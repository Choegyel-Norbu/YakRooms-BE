package com.yakrooms.be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    private final Environment env;

    public FirebaseConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
public void init() {
    try {
        FirebaseOptions options;

        if (env.matchesProfiles("prod")) {
            String encodedCredentials = env.getRequiredProperty("FIREBASE_CONFIG_BASE64");
            byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials);
            InputStream credentialsStream = new ByteArrayInputStream(decodedBytes);
            options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .build();
        } else {
            InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");
            options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        }

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase Initialized");
        }

    } catch (Exception e) {
        System.err.println("❌ Firebase init failed: " + e.getMessage());
        e.printStackTrace();
    }
}

}