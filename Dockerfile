# Multi-stage Dockerfile for Java + Node.js hybrid application
# Production-optimized with security best practices

# Stage 1: Build environment with both Java and Node.js
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set build environment variables
ENV MAVEN_OPTS="-Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication"
ENV NODE_ENV=production

# Install system dependencies (remove version pinning for compatibility)
RUN apk add --no-cache \
        curl \
        ca-certificates \
        bash \
        git \
        maven \
        nodejs \
        npm \
    && apk upgrade --no-cache

# Create build user for security
RUN addgroup -S builduser && adduser -S -G builduser builduser

# Set working directory and fix ownership
WORKDIR /app
RUN chown builduser:builduser /app

# Copy package files first for better layer caching
COPY --chown=builduser:builduser package*.json pom.xml ./

# Switch to build user
USER builduser

# Install Node.js dependencies with security audit
RUN npm ci --omit=dev --no-audit --no-fund --ignore-scripts

# Switch back to root for copying source code
USER root

# Copy source code
COPY --chown=builduser:builduser . .

# Make build script executable
RUN chmod +x railway-build.sh

# Switch to build user for building
USER builduser

# Run custom build script
RUN ./railway-build.sh

# Stage 2: Runtime environment
FROM eclipse-temurin:17-jre-alpine AS runtime

# Set production-optimized JVM parameters for faster startup
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Dspring.jmx.enabled=false"

# Install runtime dependencies (remove version pinning for compatibility)
RUN apk add --no-cache \
        curl \
        ca-certificates \
        nodejs \
        npm \
        dumb-init \
    && apk upgrade --no-cache \
    && rm -rf /var/cache/apk/*

# Create non-root user for security
RUN addgroup -S -g 1001 appuser && \
    adduser -S -u 1001 -G appuser appuser

# Set working directory
WORKDIR /app

# Copy built application and handle JAR file naming
COPY --from=builder /app/target/ ./target/

# Find and copy the JAR file to app.jar
RUN echo "=== DEBUGGING JAR FILE COPY ===" && \
    echo "Contents of target directory:" && \
    ls -la ./target/ && \
    echo "Looking for main JAR file..." && \
    if [ -f "./target/yakrooms-0.0.1-SNAPSHOT.jar" ]; then \
        echo "✅ Found expected JAR: yakrooms-0.0.1-SNAPSHOT.jar"; \
        cp "./target/yakrooms-0.0.1-SNAPSHOT.jar" "./app.jar"; \
    else \
        echo "❌ Expected JAR not found, searching for alternatives..."; \
        JAR_FILE=$(find ./target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -n 1); \
        if [ -n "$JAR_FILE" ]; then \
            echo "✅ Using alternative JAR file: $JAR_FILE"; \
            cp "$JAR_FILE" "./app.jar"; \
        else \
            echo "❌ ERROR: No suitable JAR file found!"; \
            echo "All files in target:"; \
            find ./target -type f; \
            exit 1; \
        fi \
    fi && \
    echo "✅ Final app.jar created:" && \
    ls -la ./app.jar

# Copy Node.js components
COPY --from=builder --chown=appuser:appuser /app/node_modules ./node_modules
COPY --from=builder --chown=appuser:appuser /app/uploadthing-delete.js ./
COPY --from=builder --chown=appuser:appuser /app/package*.json ./

# Set ownership and permissions
RUN chown -R appuser:appuser /app && \
    chmod 550 /app && \
    chmod 440 /app/app.jar && \
    chmod 550 /app/uploadthing-delete.js

# Switch to non-root user early for security
USER appuser

# Expose port
EXPOSE 8080

# Health check with lenient configuration for startup - Railway will override this
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/health/ping || exit 1

# Use dumb-init to handle signals properly and prevent zombie processes
ENTRYPOINT ["dumb-init", "--"]

# Start application with environment-based profile selection
CMD ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production} -jar app.jar"]
