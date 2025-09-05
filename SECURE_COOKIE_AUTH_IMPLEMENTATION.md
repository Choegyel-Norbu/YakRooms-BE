# Secure Cookie Authentication Implementation

## Overview

This implementation extends your existing Google OAuth2 authentication system with a production-ready secure authentication mechanism using JWT tokens and HttpOnly cookies. The system follows security best practices and implements token rotation for enhanced security.

## Architecture

### Dual-Token System
- **Access Tokens**: Short-lived (15 minutes) for API requests
- **Refresh Tokens**: Long-lived (7 days) stored securely in database
- **Token Rotation**: Refresh tokens are rotated on each use for enhanced security

### Security Features
- **HttpOnly Cookies**: Prevents XSS attacks
- **Secure Cookies**: HTTPS-only transmission in production
- **SameSite=Strict**: Prevents CSRF attacks
- **Token Hashing**: Refresh tokens are hashed before database storage
- **Device Tracking**: IP and device information for security monitoring
- **Automatic Cleanup**: Expired tokens are automatically cleaned up

## Implementation Details

### 1. RefreshToken Entity (`RefreshToken.java`)
```java
@Entity
@Table(name = "refresh_tokens", indexes = {...})
public class RefreshToken {
    private Long id;
    private String tokenHash;        // SHA-256 hash of the token
    private Long userId;
    private LocalDateTime expiresAt;
    private Boolean isRevoked;
    private LocalDateTime revokedAt;
    private String deviceInfo;       // User-Agent for tracking
    private String ipAddress;        // Client IP for security
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Security Considerations:**
- Tokens are hashed before storage (never store plain tokens)
- Automatic expiration cleanup via database constraints
- Audit fields for security monitoring
- Indexed queries for fast lookups

### 2. Enhanced JWT Utilities (`JwtUtil.java`)
```java
@Component
public class JwtUtil {
    // Configuration-driven token expiration
    private final long accessTokenExpirationMs;  // 15 minutes
    private final long refreshTokenExpirationMs; // 7 days
    
    // Token generation with proper claims
    public String generateAccessToken(User user);
    public String generateRefreshToken(User user);
    
    // Token validation with type checking
    public boolean validateAccessToken(String token);
    public boolean validateRefreshToken(String token);
    
    // Secure token hashing for storage
    public String generateTokenHash(String token);
}
```

**Security Features:**
- Configurable expiration times via properties
- Token type validation (access vs refresh)
- Secure HMAC-SHA512 signing
- SHA-256 hashing for refresh token storage

### 3. Cookie Management (`CookieUtil.java`)
```java
@Component
public class CookieUtil {
    // Secure cookie creation
    public void setAccessTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds);
    public void setRefreshTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds);
    
    // Cookie extraction
    public String getAccessTokenFromCookie(HttpServletRequest request);
    public String getRefreshTokenFromCookie(HttpServletRequest request);
    
    // Cookie cleanup
    public void clearAllTokenCookies(HttpServletResponse response);
}
```

**Security Features:**
- HttpOnly cookies prevent XSS attacks
- Secure flag ensures HTTPS-only transmission
- Proper path settings (access: "/", refresh: "/refresh-token")
- Automatic expiration handling

### 4. Refresh Token Service (`RefreshTokenService.java`)
```java
@Service
public interface RefreshTokenService {
    // Token creation with security tracking
    RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress);
    
    // Token rotation for enhanced security
    RefreshToken validateAndRotateToken(String token, String deviceInfo, String ipAddress);
    
    // Token revocation
    int revokeAllUserTokens(Long userId);
    boolean revokeToken(String tokenHash);
    
    // Maintenance operations
    int cleanupExpiredTokens();
    long getActiveTokenCount(Long userId);
}
```

**Security Features:**
- Token rotation on each refresh
- Rate limiting based on active token count
- Device and IP tracking for security monitoring
- Automatic cleanup of expired tokens

### 5. Updated Authentication Flow

#### Login Flow (`/auth/firebase`)
1. Client sends Google ID token
2. Server verifies token with Firebase
3. Server creates/updates user in database
4. Server generates access token (15 minutes)
5. Server generates refresh token (7 days)
6. Server sets both tokens as secure HttpOnly cookies
7. Server returns user information

#### Token Refresh Flow (`/auth/refresh-token`)
1. Client sends request with refresh token cookie
2. Server validates refresh token
3. Server rotates refresh token (old token revoked, new token created)
4. Server generates new access token
5. Server sets new tokens as secure cookies
6. Server returns success response

#### Logout Flow (`/auth/logout`)
1. Client sends logout request
2. Server revokes all refresh tokens for user
3. Server clears all authentication cookies
4. Server returns success response

### 6. Security Configuration Updates

#### CSRF Protection
```java
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .ignoringRequestMatchers("/auth/firebase", "/auth/refresh-token", "/auth/logout")
)
```

#### Cookie Security
- HttpOnly: Prevents XSS attacks
- Secure: HTTPS-only in production
- SameSite: Strict for CSRF protection
- Proper paths: Access token ("/"), Refresh token ("/refresh-token")

## Configuration Properties

### JWT Configuration
```properties
# JWT token settings
jwt.secret=${JWT_SECRET:default-secret-key-change-in-production}
jwt.access-token-expiration=900000          # 15 minutes
jwt.refresh-token-expiration=604800000     # 7 days
jwt.refresh-token.max-per-user=5          # Rate limiting
jwt.refresh-token.cleanup-batch-size=100  # Cleanup batch size
```

### Cookie Configuration
```properties
# Secure cookie settings
app.cookies.secure=${COOKIE_SECURE:true}   # HTTPS-only in production
app.cookies.domain=${COOKIE_DOMAIN:}       # Domain restriction
```

## API Endpoints

### Authentication Endpoints
- `POST /auth/firebase` - Google OAuth2 login with secure cookies
- `POST /auth/refresh-token` - Refresh access token with rotation
- `POST /auth/logout` - Logout and revoke all tokens

### Request/Response Examples

#### Login Request
```json
POST /auth/firebase
{
  "idToken": "google-firebase-id-token"
}
```

#### Login Response
```json
{
  "token": "access-token",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "roles": ["GUEST"]
  }
}
```

#### Refresh Token Request
```http
POST /auth/refresh-token
Cookie: refresh_token=refresh-token-value
```

#### Refresh Token Response
```json
{
  "message": "Token refreshed successfully",
  "accessTokenExpiresIn": 900,
  "refreshTokenExpiresIn": 604800
}
```

#### Logout Request
```http
POST /auth/logout
Cookie: access_token=access-token-value; refresh_token=refresh-token-value
```

#### Logout Response
```json
{
  "message": "Logged out successfully",
  "revokedTokens": 3
}
```

## Security Best Practices Implemented

### 1. Token Security
- ✅ Short-lived access tokens (15 minutes)
- ✅ Long-lived refresh tokens (7 days)
- ✅ Token rotation on refresh
- ✅ Secure token hashing before storage
- ✅ Token type validation

### 2. Cookie Security
- ✅ HttpOnly cookies prevent XSS
- ✅ Secure flag for HTTPS-only transmission
- ✅ SameSite=Strict prevents CSRF
- ✅ Proper path restrictions
- ✅ Automatic expiration

### 3. CSRF Protection
- ✅ CSRF tokens for state-changing operations
- ✅ SameSite cookie attribute
- ✅ Proper CORS configuration

### 4. Security Monitoring
- ✅ Device information tracking
- ✅ IP address logging
- ✅ Token usage monitoring
- ✅ Automatic cleanup of expired tokens

### 5. Rate Limiting
- ✅ Maximum tokens per user (5)
- ✅ Automatic cleanup of oldest tokens
- ✅ Batch cleanup operations

## Database Schema

### Refresh Tokens Table
```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at DATETIME,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    
    INDEX idx_refresh_token_hash (token_hash),
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_expires (expires_at)
);
```

## Deployment Considerations

### Environment Variables
```bash
# Production environment variables
JWT_SECRET=your-super-secure-jwt-secret-key-here
COOKIE_SECURE=true
COOKIE_DOMAIN=yourdomain.com
```

### Security Headers
The implementation includes comprehensive security headers:
- HSTS (HTTP Strict Transport Security)
- Content Security Policy
- X-Frame-Options
- X-Content-Type-Options
- Referrer Policy

### Monitoring and Maintenance
- Monitor active token counts per user
- Set up alerts for suspicious authentication patterns
- Regular cleanup of expired tokens
- Monitor failed authentication attempts

## Testing Strategy

### Unit Tests
- JWT token generation and validation
- Cookie creation and extraction
- Token rotation logic
- Security validation

### Integration Tests
- Complete authentication flow
- Token refresh flow
- Logout flow
- Security header validation

### Security Tests
- XSS prevention (HttpOnly cookies)
- CSRF protection
- Token expiration handling
- Rate limiting validation

## Migration from Current System

### Backward Compatibility
- Existing Authorization header support maintained
- Gradual migration to cookie-based authentication
- Legacy token generation methods preserved

### Migration Steps
1. Deploy new authentication system
2. Update frontend to use cookie-based authentication
3. Monitor authentication patterns
4. Remove Authorization header support (optional)

## Performance Considerations

### Database Optimization
- Indexed queries for fast token lookups
- Batch operations for cleanup
- Connection pooling for high concurrency

### Caching Strategy
- User information caching
- Token validation caching (optional)
- Redis integration for session management

### Scalability
- Stateless authentication
- Horizontal scaling support
- Database sharding considerations

## Conclusion

This implementation provides a production-ready, secure authentication system that:

1. **Enhances Security**: Implements industry-standard security practices
2. **Maintains Compatibility**: Works with existing Google OAuth2 flow
3. **Provides Flexibility**: Configurable token expiration and security settings
4. **Ensures Scalability**: Stateless design supports horizontal scaling
5. **Includes Monitoring**: Comprehensive security tracking and logging

The system is ready for production deployment and provides a solid foundation for secure user authentication in your Spring Boot application.
