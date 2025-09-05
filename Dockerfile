# Multi-stage Dockerfile for Java + Node.js hybrid application
# This explicitly defines our build process to avoid nixpacks auto-detection issues

# Stage 1: Build environment with both Java and Node.js
FROM eclipse-temurin:17-jdk AS builder

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive
ENV MAVEN_OPTS="-Xmx1024m"

# Install system dependencies with IPv6 fallback
RUN echo "Acquire::ForceIPv4 \"true\";" > /etc/apt/apt.conf.d/99force-ipv4 && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        ca-certificates \
        gnupg \
        lsb-release && \
    rm -rf /var/lib/apt/lists/*

# Install Node.js 18.x (LTS)
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

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
FROM eclipse-temurin:17-jre AS runtime

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Install Node.js runtime (for UploadThing scripts)
RUN echo "Acquire::ForceIPv4 \"true\";" > /etc/apt/apt.conf.d/99force-ipv4 && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        ca-certificates && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

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
