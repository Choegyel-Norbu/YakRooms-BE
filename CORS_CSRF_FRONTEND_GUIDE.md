# CORS & CSRF Configuration Guide for Frontend

## 🎯 **Problem Solved**
Fixed CORS preflight failures with CSRF tokens that were preventing frontend authentication.

## 🔧 **Backend Changes Made**

### **1. Enhanced CORS Configuration**
Updated both `CorsConfig.java` and `SecurityConfig.java` to include CSRF token headers:

```java
// Allowed headers now include:
"X-XSRF-TOKEN",  // Spring Security CSRF token header
"X-CSRF-TOKEN"   // Alternative CSRF token header
```

### **2. CSRF Token Repository**
Using `CookieCsrfTokenRepository.withHttpOnlyFalse()` to make CSRF tokens accessible to JavaScript.

### **3. Auth Endpoints Exempt**
Authentication endpoints are exempt from CSRF validation:
- `/auth/firebase`
- `/auth/refresh-token` 
- `/auth/logout`

## 📋 **Frontend Implementation Requirements**

### **1. HTTP Client Configuration**
```javascript
// Axios example
const apiClient = axios.create({
  baseURL: 'https://your-backend-url.com',
  withCredentials: true,  // CRITICAL: Include cookies
  headers: {
    'Content-Type': 'application/json',
  }
});

// Add CSRF token to requests
apiClient.interceptors.request.use((config) => {
  const csrfToken = getCsrfTokenFromCookie(); // Get from XSRF-TOKEN cookie
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken;
  }
  return config;
});
```

### **2. CSRF Token Handling**
```javascript
// Function to get CSRF token from cookie
function getCsrfTokenFromCookie() {
  const cookies = document.cookie.split(';');
  for (let cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'XSRF-TOKEN') {
      return decodeURIComponent(value);
    }
  }
  return null;
}
```

### **3. Error Handling**
```javascript
// Handle CORS/CSRF errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 403) {
      // CSRF token issue - refresh token and retry
      console.error('CSRF token validation failed');
      // Optionally refresh CSRF token
    }
    if (error.code === 'ERR_NETWORK') {
      // CORS preflight failure
      console.error('CORS preflight failed - check headers');
    }
    return Promise.reject(error);
  }
);
```

## 🧪 **Testing Checklist**

### **✅ CORS Preflight Test**
```bash
curl -X OPTIONS \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: X-XSRF-TOKEN,Content-Type" \
  https://your-backend-url.com/auth/firebase
```

### **✅ CSRF Token Test**
```bash
# 1. Get CSRF token
curl -c cookies.txt https://your-backend-url.com/auth/firebase

# 2. Use CSRF token in request
curl -b cookies.txt \
  -H "X-XSRF-TOKEN: <token-from-cookie>" \
  -X POST https://your-backend-url.com/api/some-endpoint
```

## 🚨 **Common Issues & Solutions**

### **Issue 1: CORS Preflight Failure**
**Error**: `Access to XMLHttpRequest at 'backend-url' from origin 'frontend-url' has been blocked by CORS policy`

**Solution**: 
- Ensure `withCredentials: true` is set
- Check that `X-XSRF-TOKEN` header is in allowed headers
- Verify origin is in allowed origin patterns

### **Issue 2: CSRF Token Missing**
**Error**: `403 Forbidden` or `Invalid CSRF token`

**Solution**:
- Include `X-XSRF-TOKEN` header in requests
- Get token from `XSRF-TOKEN` cookie
- Ensure cookie is accessible (not HttpOnly)

### **Issue 3: Header Name Inconsistency**
**Error**: Different systems use different header names

**Solution**: 
- Backend supports both `X-XSRF-TOKEN` and `X-CSRF-TOKEN`
- Use `X-XSRF-TOKEN` (Spring Security standard)
- Fallback to `X-CSRF-TOKEN` if needed

## 📊 **Backend CORS Configuration Summary**

```java
// Allowed Origins
"http://localhost:*"
"https://localhost:*" 
"http://127.0.0.1:*"
"https://yak-rooms-fe.vercel.app"
"https://*.vercel.app"
"https://*.ngrok-free.app"

// Allowed Headers
"Authorization", "Content-Type", "X-Requested-With", 
"Accept", "Origin", "Access-Control-Request-Method",
"Access-Control-Request-Headers", "X-XSRF-TOKEN", "X-CSRF-TOKEN"

// Allowed Methods
"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"

// Credentials
allowCredentials: true
```

## 🎉 **Result**
Frontend can now successfully:
- ✅ Make authenticated requests with cookies
- ✅ Include CSRF tokens in headers
- ✅ Handle CORS preflight requests
- ✅ Work with both header name variations
- ✅ Get clear error messages for debugging

The authentication flow should now work seamlessly between frontend and backend!
