# Multi-stage Dockerfile for Java + Node.js hybrid application
# This explicitly defines our build process to avoid nixpacks auto-detection issues

# Stage 1: Build environment with both Java and Node.js
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set environment variables
ENV MAVEN_OPTS="-Xmx1024m"

# Install system dependencies (Alpine uses apk instead of apt)
RUN apk add --no-cache \
        curl \
        ca-certificates \
        bash \
        git \
        maven \
        nodejs \
        npm

# Set working directory
WORKDIR /app

# Copy package files first for better layer caching
COPY package*.json ./
COPY pom.xml ./

# Install Node.js dependencies
RUN npm ci --only=production --no-audit --no-fund

# Copy source code
COPY . .

# Make build script executable
RUN chmod +x railway-build.sh

# Run custom build script
RUN ./railway-build.sh

# Stage 2: Runtime environment
FROM eclipse-temurin:17-jre-alpine AS runtime

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Install Node.js runtime (for UploadThing scripts)
RUN apk add --no-cache \
        curl \
        ca-certificates \
        nodejs \
        npm

# Create non-root user for security
RUN addgroup -S appuser && adduser -S -G appuser appuser

# Set working directory
WORKDIR /app

# Copy built application from builder stage
COPY --from=builder /app/target/*.jar ./app.jar
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/uploadthing-delete.js ./
COPY --from=builder /app/package*.json ./

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Start application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
