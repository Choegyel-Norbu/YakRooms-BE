# UploadThing URL Usage Examples

## Overview

This guide demonstrates how to use the UploadThing integration with URLs stored in your database. The system automatically extracts file keys from UploadThing URLs and handles the deletion process.

## Your File URL Example

Given your file URL: `https://utfs.io/f/afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg`

The system will automatically extract the file key: `afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg`

## API Endpoints for URL-Based Deletion

### 1. Delete Single File by URL

```http
DELETE /api/v1/uploadthing/files/url
Content-Type: application/json

"https://utfs.io/f/afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg"
```

**Response:**
```json
{
  "success": true,
  "message": "File deleted successfully",
  "deletedFiles": ["afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg"],
  "failedFiles": []
}
```

### 2. Delete Multiple Files by URLs

```http
DELETE /api/v1/uploadthing/files/urls
Content-Type: application/json

[
  "https://utfs.io/f/afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg",
  "https://utfs.io/f/another-file-key-12345.png",
  "https://utfs.io/f/third-file-key-67890.pdf"
]
```

**Response:**
```json
{
  "success": true,
  "message": "Files deleted successfully",
  "deletedFiles": [
    "afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg",
    "another-file-key-12345.png",
    "third-file-key-67890.pdf"
  ],
  "failedFiles": []
}
```

### 3. Delete Files by URLs Asynchronously

```http
DELETE /api/v1/uploadthing/files/urls/async
Content-Type: application/json

[
  "https://utfs.io/f/async-file1.jpeg",
  "https://utfs.io/f/async-file2.png"
]
```

## Service Layer Usage

### Delete Single File from Database

```java
@Service
public class UserService {
    
    @Autowired
    private UploadThingService uploadThingService;
    
    public void deleteUserProfileImage(String imageUrl) {
        try {
            UploadThingDeleteResponse response = uploadThingService.deleteFileByUrl(imageUrl);
            
            if (response.isSuccess()) {
                log.info("Successfully deleted user profile image: {}", imageUrl);
            } else {
                log.warn("Failed to delete user profile image: {}", response.getMessage());
            }
        } catch (BusinessException e) {
            log.error("Failed to delete user profile image: {}", e.getMessage());
            throw e;
        }
    }
}
```

### Delete Multiple Files from Database

```java
@Service
public class HotelService {
    
    @Autowired
    private UploadThingService uploadThingService;
    
    public void deleteHotelImages(List<String> imageUrls) {
        try {
            UploadThingDeleteResponse response = uploadThingService.deleteFilesByUrls(imageUrls);
            
            if (response.isSuccess()) {
                log.info("Successfully deleted {} hotel images", 
                    response.getDeletedFiles() != null ? response.getDeletedFiles().size() : 0);
                
                if (response.getFailedFiles() != null && !response.getFailedFiles().isEmpty()) {
                    log.warn("Failed to delete {} images: {}", 
                        response.getFailedFiles().size(), response.getFailedFiles());
                }
            } else {
                log.error("Failed to delete hotel images: {}", response.getMessage());
            }
        } catch (BusinessException e) {
            log.error("Failed to delete hotel images: {}", e.getMessage());
            throw e;
        }
    }
}
```

### Async Deletion for Better Performance

```java
@Service
public class BookingService {
    
    @Autowired
    private UploadThingService uploadThingService;
    
    public void deleteBookingDocumentsAsync(List<String> documentUrls) {
        CompletableFuture<UploadThingDeleteResponse> future = 
            uploadThingService.deleteFilesByUrlsAsync(documentUrls);
        
        future.thenAccept(response -> {
            if (response.isSuccess()) {
                log.info("Async deletion completed successfully for {} documents", 
                    response.getDeletedFiles() != null ? response.getDeletedFiles().size() : 0);
            } else {
                log.error("Async deletion failed: {}", response.getMessage());
            }
        }).exceptionally(throwable -> {
            log.error("Async deletion failed with exception", throwable);
            return null;
        });
    }
}
```

## Database Integration Examples

### JPA Entity with File URLs

```java
@Entity
@Table(name = "hotel_images")
public class HotelImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
    
    // getters and setters
}
```

### Service to Delete Hotel Images

```java
@Service
public class HotelImageService {
    
    @Autowired
    private HotelImageRepository hotelImageRepository;
    
    @Autowired
    private UploadThingService uploadThingService;
    
    @Transactional
    public void deleteHotelImages(Long hotelId) {
        // Get all image URLs for the hotel
        List<String> imageUrls = hotelImageRepository
            .findByHotelId(hotelId)
            .stream()
            .map(HotelImage::getImageUrl)
            .collect(Collectors.toList());
        
        if (!imageUrls.isEmpty()) {
            // Delete files from UploadThing
            UploadThingDeleteResponse response = uploadThingService.deleteFilesByUrls(imageUrls);
            
            if (response.isSuccess()) {
                // Delete database records
                hotelImageRepository.deleteByHotelId(hotelId);
                log.info("Successfully deleted {} hotel images and database records", 
                    response.getDeletedFiles().size());
            } else {
                log.error("Failed to delete hotel images: {}", response.getMessage());
                throw new BusinessException("Failed to delete hotel images");
            }
        }
    }
}
```

## Error Handling

### Invalid URL Handling

```java
@Service
public class FileCleanupService {
    
    @Autowired
    private UploadThingService uploadThingService;
    
    public void cleanupInvalidFiles(List<String> fileUrls) {
        List<String> validUrls = new ArrayList<>();
        List<String> invalidUrls = new ArrayList<>();
        
        // Validate URLs before processing
        for (String url : fileUrls) {
            if (UploadThingUrlUtil.isValidUploadThingUrl(url)) {
                validUrls.add(url);
            } else {
                invalidUrls.add(url);
                log.warn("Invalid UploadThing URL found: {}", url);
            }
        }
        
        if (!validUrls.isEmpty()) {
            try {
                UploadThingDeleteResponse response = uploadThingService.deleteFilesByUrls(validUrls);
                log.info("Cleanup completed. Valid files: {}, Invalid URLs: {}", 
                    validUrls.size(), invalidUrls.size());
            } catch (BusinessException e) {
                log.error("File cleanup failed: {}", e.getMessage());
            }
        }
    }
}
```

## Testing with Your URL

### Using curl

```bash
# Test with your specific URL
curl -X DELETE "http://localhost:8080/api/v1/uploadthing/files/url" \
  -H "Content-Type: application/json" \
  -d '"https://utfs.io/f/afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg"'
```

### Using the Test Script

```bash
# Run the updated test script
./test_uploadthing.sh
```

## URL Validation

The system automatically validates UploadThing URLs:

- ✅ Valid: `https://utfs.io/f/afbf548f-9ea6-4c33-af30-e99faa902dfc-43rtxt.jpeg`
- ✅ Valid with query params: `https://utfs.io/f/file.jpeg?width=800&height=600`
- ❌ Invalid domain: `https://example.com/f/file.jpeg`
- ❌ Invalid path: `https://utfs.io/invalid/path/file.jpeg`
- ❌ Invalid format: `not-a-valid-url`

## Performance Considerations

1. **Batch Processing**: Use the multiple files endpoints for better performance
2. **Async Operations**: Use async methods for non-blocking operations
3. **Error Handling**: Always handle partial failures gracefully
4. **Validation**: Validate URLs before processing to avoid unnecessary API calls

## Security Notes

1. **URL Validation**: The system validates URLs to ensure they're from UploadThing
2. **Error Messages**: Error messages don't expose sensitive information
3. **Logging**: URLs are logged for debugging but can be configured for production

## Next Steps

1. **Start your application**: `mvn spring-boot:run`
2. **Test with your URL**: Use the curl command above
3. **Integrate with your services**: Add the service methods to your existing services
4. **Monitor logs**: Check application logs for operation details

Your UploadThing integration is now ready to handle URLs directly from your database!
