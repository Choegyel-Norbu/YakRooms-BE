# Frontend Authentication Implementation Prompt

## 🎯 **Objective**
Implement secure cookie-based authentication for YakRooms frontend application using HttpOnly cookies instead of Authorization headers.

## 🔗 **API Endpoints**

### **Authentication Endpoints**
- `POST /auth/firebase` - Google OAuth login (returns user data, sets cookies)
- `POST /auth/refresh-token` - Refresh access token (uses refresh token cookie)
- `POST /auth/logout` - Logout user (clears all cookies)

### **Protected Endpoints**
- `GET /api/user/profile` - Get current user profile (requires authentication)

## 📋 **Implementation Instructions**

### **1. HTTP Client Configuration**
- Set `withCredentials: true` for all requests
- Add automatic token refresh on 401 errors
- Include CSRF token in headers

### **2. Authentication Service**
- Remove localStorage token storage
- Use cookies automatically (handled by browser)
- Implement login/logout/refresh methods
- Handle authentication errors properly

### **3. React Context**
- Create AuthProvider with user state
- Implement login/logout functions
- Add loading states
- Handle authentication status checks

### **4. Components**
- Login component with Google OAuth
- Protected route wrapper
- Logout functionality

## 🔧 **Key Requirements**
- Use `withCredentials: true` in all HTTP requests
- Remove Authorization header approach
- Implement automatic token refresh
- Handle CSRF tokens
- No manual token storage in localStorage

## 🧪 **Testing**
- Test login/logout flows
- Verify cookie handling
- Test protected routes
- Check automatic token refresh
