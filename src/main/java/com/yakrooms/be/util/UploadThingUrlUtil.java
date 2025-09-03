package com.yakrooms.be.util;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for handling UploadThing URLs and file keys.
 * Provides methods to extract file keys from UploadThing URLs.
 */
public class UploadThingUrlUtil {

    private static final String UPLOADTHING_DOMAIN = "utfs.io";
    private static final String UPLOADTHING_PATH_PREFIX = "/f/";

    /**
     * Extracts the file key from an UploadThing URL.
     * 
     * @param url The UploadThing URL (e.g., https://utfs.io/f/afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg)
     * @return The file key (e.g., afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg)
     * @throws IllegalArgumentException if the URL is not a valid UploadThing URL
     */
    public static String extractFileKey(String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        try {
            URI uri = new URI(url);
            
            // Check if it's an UploadThing URL
            if (!UPLOADTHING_DOMAIN.equals(uri.getHost())) {
                throw new IllegalArgumentException("Not a valid UploadThing URL: " + url);
            }

            String path = uri.getPath();
            if (path == null || !path.startsWith(UPLOADTHING_PATH_PREFIX)) {
                throw new IllegalArgumentException("Invalid UploadThing URL format: " + url);
            }

            // Extract file key from path
            String fileKey = path.substring(UPLOADTHING_PATH_PREFIX.length());
            
            if (!StringUtils.hasText(fileKey)) {
                throw new IllegalArgumentException("No file key found in URL: " + url);
            }

            return fileKey;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + url, e);
        }
    }

    /**
     * Extracts file keys from a list of UploadThing URLs.
     * 
     * @param urls List of UploadThing URLs
     * @return List of file keys
     * @throws IllegalArgumentException if any URL is invalid
     */
    public static List<String> extractFileKeys(List<String> urls) {
        if (urls == null) {
            throw new IllegalArgumentException("URLs list cannot be null");
        }

        return urls.stream()
                .map(UploadThingUrlUtil::extractFileKey)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a URL is a valid UploadThing URL.
     * 
     * @param url The URL to check
     * @return true if it's a valid UploadThing URL, false otherwise
     */
    public static boolean isValidUploadThingUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }

        try {
            URI uri = new URI(url);
            return UPLOADTHING_DOMAIN.equals(uri.getHost()) && 
                   uri.getPath() != null && 
                   uri.getPath().startsWith(UPLOADTHING_PATH_PREFIX);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Builds an UploadThing URL from a file key.
     * 
     * @param fileKey The file key
     * @return The complete UploadThing URL
     */
    public static String buildUrl(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            throw new IllegalArgumentException("File key cannot be null or empty");
        }
        return "https://" + UPLOADTHING_DOMAIN + UPLOADTHING_PATH_PREFIX + fileKey;
    }
}
