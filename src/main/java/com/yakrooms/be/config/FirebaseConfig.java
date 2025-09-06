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
            // Allow application to start even if Firebase configuration is missing
            if (FirebaseApp.getApps().isEmpty()) {
                initializeFirebase();
            } else {
                logger.info("Firebase already initialized");
            }
        } catch (Exception e) {
            logger.warn("Firebase initialization failed - application will continue without Firebase: {}", e.getMessage());
        }
    }

    private void initializeFirebase() throws Exception {
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
                        logger.warn("Firebase credentials not found. Set FIREBASE_CONFIG_BASE64 environment variable for production.");
                        logger.warn("⚠️  Firebase initialization skipped - some features may not work properly");
                        return; // Skip Firebase initialization instead of crashing
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
                    logger.warn("Firebase service account file not found. Please ensure firebase-service-account.json exists in src/main/resources/");
                    logger.warn("⚠️  Firebase initialization skipped - some features may not work properly");
                    return; // Skip Firebase initialization instead of crashing
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
    }

    private String getActiveProfile() {
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return String.join(",", activeProfiles);
        }
        return "default";
    }

}