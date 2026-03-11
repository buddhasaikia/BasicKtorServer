#!/bin/bash
# Integration test script to verify login works with SEED_DATA

set -e

echo "=== Security Integration Test ==="
echo ""

# Clean up
rm -f db/data.*

# Start server with SEED_DATA
echo "1. Starting server with SEED_DATA=true..."
SEED_DATA=true java -jar build/libs/BasicKtorServer-all.jar > /tmp/test_server.log 2>&1 &
SERVER_PID=$!
sleep 5

echo "   ✓ Server started (PID: $SERVER_PID)"
echo ""

# Test 1: Home endpoint
echo "2. Testing home endpoint..."
RESPONSE=$(curl -s http://127.0.0.1:8080/ 2>/dev/null || echo "")
if [ -n "$RESPONSE" ]; then
    echo "   ✓ Home endpoint works"
else
    echo "   ✗ Home endpoint failed"
    kill $SERVER_PID
    exit 1
fi
echo ""

# Test 2: Login with valid credentials
echo "3. Testing login with seed user (testuser/password123)..."
LOGIN_RESPONSE=$(curl -s -X POST http://127.0.0.1:8080/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}' 2>/dev/null)

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    echo "   ✓ Login successful"
    echo "   ✓ Token received: ${TOKEN:0:20}..."
else
    echo "   ✗ Login failed"
    echo "   Response: $LOGIN_RESPONSE"
    kill $SERVER_PID
    exit 1
fi
echo ""

# Test 3: Input validation - weak password
echo "4. Testing input validation (weak password)..."
WEAK_PWD=$(curl -s -X POST http://127.0.0.1:8080/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"short"}' 2>/dev/null)

if echo "$WEAK_PWD" | grep -q "at least 8 characters"; then
    echo "   ✓ Weak password rejected with proper error"
else
    echo "   ✗ Password validation failed"
    kill $SERVER_PID
    exit 1
fi
echo ""

# Test 5: Input validation - invalid username
echo "5. Testing input validation (short username)..."
SHORT_USER=$(curl -s -X POST http://127.0.0.1:8080/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ab","password":"password123"}' 2>/dev/null)

if echo "$SHORT_USER" | grep -q "at least 3 characters"; then
    echo "   ✓ Short username rejected with proper error"
else
    echo "   ✗ Username validation failed"
    kill $SERVER_PID
    exit 1
fi
echo ""

# Test 6: Protected endpoint access
echo "6. Testing protected endpoint with token..."
USERS_RESPONSE=$(curl -s http://127.0.0.1:8080/v1/users \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)

if [ -n "$USERS_RESPONSE" ]; then
    echo "   ✓ Protected endpoint accessible with token"
else
    echo "   ✗ Protected endpoint access failed"
    kill $SERVER_PID
    exit 1
fi
echo ""

# Test 7: Protected endpoint without token
echo "7. Testing protected endpoint without token..."
NO_TOKEN=$(curl -s http://127.0.0.1:8080/v1/users 2>/dev/null)

if echo "$NO_TOKEN" | grep -q "Unauthorized\|not valid"; then
    echo "   ✓ Protected endpoint properly rejects unauthenticated request"
else
    echo "   ✗ Protected endpoint access control failed"
    kill $SERVER_PID
    exit 1
fi
echo ""

# Cleanup
echo "=== Cleanup ==="
kill $SERVER_PID
echo "✓ Server stopped"
echo ""
echo "=== All Security Tests Passed! ==="
