#!/bin/bash

# Health Check Test Script for Railway Deployment
# This script tests if the health endpoints are working correctly

echo "🔍 Testing YakRooms Health Endpoints..."
echo "================================"

# Test local health endpoint (for development)
echo ""
echo "📍 Testing LOCAL health endpoints (if running locally):"
echo "curl -s http://localhost:8080/health/ping"
LOCAL_PING=$(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/health/ping 2>/dev/null || echo "000")
if [ "$LOCAL_PING" = "200" ]; then
    echo "✅ Local /health/ping: OK"
else
    echo "❌ Local /health/ping: Failed (HTTP $LOCAL_PING) - This is expected if not running locally"
fi

echo ""
echo "curl -s http://localhost:8080/health"
LOCAL_HEALTH=$(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/health 2>/dev/null || echo "000")
if [ "$LOCAL_HEALTH" = "200" ]; then
    echo "✅ Local /health: OK"
else
    echo "❌ Local /health: Failed (HTTP $LOCAL_HEALTH) - This is expected if not running locally"
fi

# Test production endpoints (replace with your Railway URL)
echo ""
echo "📍 Testing PRODUCTION health endpoints:"
echo "Replace 'your-railway-url' with your actual Railway deployment URL"
echo ""

RAILWAY_URL="your-railway-url.railway.app"
echo "🌐 Testing: https://$RAILWAY_URL/health/ping"
PROD_PING=$(curl -s -w "%{http_code}" -o /dev/null https://$RAILWAY_URL/health/ping 2>/dev/null || echo "000")
if [ "$PROD_PING" = "200" ]; then
    echo "✅ Production /health/ping: OK"
elif [ "$PROD_PING" = "000" ]; then
    echo "⚠️  Production /health/ping: Cannot connect - Update RAILWAY_URL or check if deployed"
else
    echo "❌ Production /health/ping: Failed (HTTP $PROD_PING)"
fi

echo ""
echo "🌐 Testing: https://$RAILWAY_URL/health"
PROD_HEALTH=$(curl -s -w "%{http_code}" -o /dev/null https://$RAILWAY_URL/health 2>/dev/null || echo "000")
if [ "$PROD_HEALTH" = "200" ]; then
    echo "✅ Production /health: OK"
elif [ "$PROD_HEALTH" = "000" ]; then
    echo "⚠️  Production /health: Cannot connect - Update RAILWAY_URL or check if deployed"
else
    echo "❌ Production /health: Failed (HTTP $PROD_HEALTH)"
fi

echo ""
echo "📊 Summary:"
echo "============"
if [ "$PROD_PING" = "200" ] && [ "$PROD_HEALTH" = "200" ]; then
    echo "🎉 All production health checks passed! Deployment is successful."
elif [ "$PROD_PING" = "000" ]; then
    echo "⚠️  Cannot connect to production URL. Update the RAILWAY_URL variable."
else
    echo "❌ Health checks failed. Check deployment logs and configuration."
fi

echo ""
echo "📝 Next steps:"
echo "1. Update RAILWAY_URL in this script with your actual Railway URL"
echo "2. Run this script after each deployment to verify health"
echo "3. Check Railway logs if health checks fail"
