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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    private final Environment env;

    public FirebaseConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        try {
            FirebaseOptions options;
            String activeProfile = getActiveProfile();

            if (env.matchesProfiles("production", "prod")) {
                // Production environment: Use Base64 encoded credentials
                String encodedCredentials = env.getProperty("FIREBASE_CONFIG_BASE64");
                if (encodedCredentials != null && !encodedCredentials.isEmpty()) {
                    logger.info("Initializing Firebase for PRODUCTION with Base64 credentials");
                    byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials);
                    InputStream credentialsStream = new ByteArrayInputStream(decodedBytes);
                    options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                        .build();
                } else {
                    logger.warn("FIREBASE_CONFIG_BASE64 not found, falling back to service account file");
                    InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");
                    if (serviceAccount == null) {
                        throw new RuntimeException("Firebase credentials not found. Set FIREBASE_CONFIG_BASE64 environment variable for production.");
                    }
                    options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                }
            } else {
                // Development environment: Use local service account file
                logger.info("Initializing Firebase for DEVELOPMENT with service account file");
                InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");
                if (serviceAccount == null) {
                    throw new RuntimeException("Firebase service account file not found. Please ensure firebase-service-account.json exists in src/main/resources/");
                }
                options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            }

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully for profile: {}", activeProfile);
            } else {
                logger.debug("Firebase already initialized");
            }

        } catch (Exception e) {
            logger.error("Firebase initialization failed: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
            // Firebase might not be critical for all environments
        }
    }

    private String getActiveProfile() {
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return String.join(",", activeProfiles);
        }
        return "default";
    }

}