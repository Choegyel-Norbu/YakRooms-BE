#!/bin/bash

# Railway build script for Java + Node.js hybrid
set -e  # Exit on any error

echo "🚀 Starting Railway build process..."

# Check if Node.js is available
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Installing Node.js..."
    apk add --no-cache nodejs npm
fi

# Check if npm is available
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Installing npm..."
    apk add --no-cache npm
fi

# Display versions
echo "📋 Environment Information:"
echo "Node.js version: $(node --version)"
echo "npm version: $(npm --version)"
echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "Maven version: $(mvn --version | head -n 1)"

# Install Node.js dependencies
echo "📦 Installing Node.js dependencies..."
npm install --production

# Verify Node.js dependencies
echo "🔍 Verifying Node.js dependencies..."
if [ -d "node_modules/uploadthing" ]; then
    echo "✅ UploadThing package installed successfully"
else
    echo "❌ UploadThing package not found"
    exit 1
fi

# Build Java application
echo "☕ Building Java application..."
mvn clean package -DskipTests

# Verify Java build
echo "🔍 Verifying Java build..."
if ls target/*.jar 1> /dev/null 2>&1; then
    echo "✅ Java application built successfully"
    echo "📦 JAR files found:"
    ls -la target/*.jar
    
    # Ensure the expected JAR file exists
    if [ -f "target/yakrooms-0.0.1-SNAPSHOT.jar" ]; then
        echo "✅ Main JAR file found: yakrooms-0.0.1-SNAPSHOT.jar"
    else
        echo "⚠️  Expected JAR name not found, but other JARs exist:"
        ls target/*.jar
    fi
else
    echo "❌ Java application build failed - no JAR files found"
    echo "Target directory contents:"
    ls -la target/ || echo "Target directory not found"
    exit 1
fi

# Verify Node.js script
echo "🔍 Verifying Node.js script..."
if [ -f "uploadthing-delete.js" ]; then
    echo "✅ Node.js script found"
else
    echo "❌ Node.js script not found"
    exit 1
fi

echo "✅ Build completed successfully!"
echo "📦 Ready for deployment with Java + Node.js hybrid solution"
