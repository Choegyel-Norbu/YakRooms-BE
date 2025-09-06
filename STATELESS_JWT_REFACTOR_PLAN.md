# Stateless JWT Refactor Plan

## Current Problem
- Storing refresh token hashes in database
- Complex token management with database dependencies
- Performance overhead on every authentication request

## Proposed Solution: Stateless JWT Approach

### 1. Remove RefreshToken Entity
- Delete `RefreshToken.java`
- Delete `RefreshTokenService` and implementation
- Remove refresh token repository

### 2. Implement Stateless Token Rotation
- Use JWT claims to track token family
- Implement token rotation without database storage
- Add optional session tracking for security monitoring

### 3. New Token Strategy
- **Access Token**: 15 minutes, contains user info
- **Refresh Token**: 7 days, contains minimal claims + rotation info
- **Token Rotation**: Generate new token pair on each refresh

### 4. Security Benefits
- No database dependency for authentication
- Better performance (no DB queries)
- Easier horizontal scaling
- Industry standard approach

### 5. Implementation Steps
1. Create new `StatelessJwtUtil` with rotation logic
2. Update `AuthController` to use stateless approach
3. Remove refresh token database dependencies
4. Add optional session tracking (without storing tokens)
5. Update security configuration

### 6. Optional: Session Tracking (Without Token Storage)
- Track active sessions for security monitoring
- Store only session metadata (user_id, device_info, last_activity)
- No token storage, just session management

## Benefits
- ✅ Simpler architecture
- ✅ Better performance
- ✅ Easier scaling
- ✅ Industry standard
- ✅ No database schema complexity
- ✅ Stateless authentication

## Trade-offs
- ❌ Cannot revoke tokens before expiration (mitigated by short token lifetime)
- ❌ No immediate token revocation (can be added with optional session tracking)
