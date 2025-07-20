package com.yakrooms.be.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.yakrooms.be.util.MultipartInputStreamFileResource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UploadThingService {

	private final RestTemplate restTemplate = new RestTemplate();

    @Value("${uploadthing.secret}")
    private String uploadThingSecret;

    @Value("${uploadthing.prepare.url}")
    private String prepareUrl;
    
    // Core method to upload all received files and return their final URLs
    public List<String> uploadFilesToUploadThing(MultipartFile[] files) {
    	
    	System.out.println("Secret key @@@: " + uploadThingSecret);
    	System.out.println("Prepare URL @@@: " + prepareUrl);
        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Step 1: Ask UploadThing for a signed URL to upload this file
                String signedUrl = requestSignedUploadUrl(file);
                
                System.out.println("Signed URL @@@: " + signedUrl);

                // Step 2: Upload the file bytes to the signed URL
                String finalFileUrl = uploadFileToSignedUrl(signedUrl, file);

                uploadedUrls.add(finalFileUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
            }
        }

        return uploadedUrls;
    }
    
 // Step 1: Request signed upload URL from UploadThing
    private String requestSignedUploadUrl(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(uploadThingSecret);
//        headers.set("Authorization", "Bearer " + uploadThingSecret);
        headers.set("x-uploadthing-api-key", uploadThingSecret); // ✅ Proper header

        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> fileMeta = Map.of(
                "name", file.getOriginalFilename(),
                "size", file.getSize(),
                "type", file.getContentType()
        );	

        Map<String, Object> requestBody = Map.of("files", List.of(fileMeta));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(prepareUrl, request, Map.class);

        Map data = (Map) ((List<?>) response.getBody().get("data")).get(0);
        return (String) data.get("url");
    }
    
 // Step 2: Upload file to UploadThing using signed URL
    private String uploadFileToSignedUrl(String signedUrl, MultipartFile file) throws Exception {
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentDispositionFormData("file", file.getOriginalFilename());
        partHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));

        // Wrap file bytes in ByteArrayResource
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(fileResource, partHeaders);

        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("file", fileEntity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(signedUrl, requestEntity, String.class);

        // After uploading, we build the final file URL
        String fileKey = signedUrl.substring(signedUrl.lastIndexOf("/") + 1);
        return "https://utfs.io/f/" + fileKey;
    }
}