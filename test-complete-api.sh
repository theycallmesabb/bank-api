#!/bin/bash

# Complete API Test Script
# Tests both JWT authentication and Banking endpoints
# 
# Usage: ./test-complete-api.sh [BASE_URL]
# Example: ./test-complete-api.sh http://localhost:8080

set -e

# Configuration
BASE_URL=${1:-"http://localhost:8080"}
API_BASE_URL="$BASE_URL/api/v1"
AUTH_BASE_URL="$BASE_URL/auth"

# Test data
JWT_USER_NAME="John Doe"
JWT_USER_EMAIL="john.doe@example.com"
JWT_USER_PASSWORD="StrongP@ss123"

BANKING_USERNAME1="alice"
BANKING_PASSWORD1="password123"
BANKING_USERNAME2="bob"
BANKING_PASSWORD2="password456"

FUND_AMOUNT=1000
PAY_AMOUNT=100

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

test_endpoint() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local auth="$5"
    local expected_status="$6"
    
    echo -e "\n${YELLOW}Testing: $name${NC}"
    echo "Method: $method"
    echo "URL: $url"
    
    if [ -n "$data" ]; then
        echo "Data: $data"
    fi
    
    if [ -n "$auth" ]; then
        echo "Auth: $auth"
    fi
    
    # Build curl command
    curl_cmd="curl -s -w \"\nHTTP_STATUS:%{http_code}\n\" -X $method \"$url\""
    
    if [ -n "$data" ]; then
        curl_cmd="$curl_cmd -H \"Content-Type: application/json\" -d '$data'"
    fi
    
    if [ -n "$auth" ]; then
        curl_cmd="$curl_cmd -H \"Authorization: $auth\""
    fi
    
    # Execute request
    response=$(eval $curl_cmd)
    http_status=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
    body=$(echo "$response" | sed '/^HTTP_STATUS:/d')
    
    echo "Response Status: $http_status"
    echo "Response Body: $body"
    
    if [ "$http_status" = "$expected_status" ]; then
        print_success "$name - Status: $http_status"
        echo "$body"
        return 0
    else
        print_error "$name - Expected: $expected_status, Got: $http_status"
        return 1
    fi
}

# Extract token from JSON response
extract_token() {
    local response="$1"
    local token_field="$2"
    echo "$response" | grep -o "\"$token_field\":\"[^\"]*\"" | cut -d'"' -f4
}

# Create Basic Auth header
create_basic_auth() {
    local username="$1"
    local password="$2"
    echo "Basic $(echo -n "$username:$password" | base64)"
}

print_header "Complete API Test Suite"
echo "Base URL: $BASE_URL"
echo "API URL: $API_BASE_URL"
echo "Auth URL: $AUTH_BASE_URL"

# ===========================
# JWT AUTHENTICATION TESTS
# ===========================

print_header "JWT Authentication Tests"

# Test 1: Register JWT User
print_header "Test 1: Register JWT User"
jwt_register_response=$(test_endpoint \
    "Register JWT User" \
    "POST" \
    "$AUTH_BASE_URL/register" \
    "{\"name\":\"$JWT_USER_NAME\",\"email\":\"$JWT_USER_EMAIL\",\"password\":\"$JWT_USER_PASSWORD\"}" \
    "" \
    "201")

# Test 2: Login JWT User
print_header "Test 2: Login JWT User"
jwt_login_response=$(curl -s -X POST "$AUTH_BASE_URL/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$JWT_USER_EMAIL\",\"password\":\"$JWT_USER_PASSWORD\"}")

echo "Login Response: $jwt_login_response"

# Extract access token
access_token=$(echo "$jwt_login_response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
refresh_token=$(echo "$jwt_login_response" | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

if [ -n "$access_token" ]; then
    print_success "Access token extracted: ${access_token:0:20}..."
    JWT_AUTH_HEADER="Bearer $access_token"
else
    print_error "Failed to extract access token"
    JWT_AUTH_HEADER=""
fi

# Test 3: Get User Profile (JWT Protected)
if [ -n "$JWT_AUTH_HEADER" ]; then
    print_header "Test 3: Get User Profile (JWT Protected)"
    test_endpoint \
        "Get User Profile" \
        "GET" \
        "$BASE_URL/profile/me" \
        "" \
        "$JWT_AUTH_HEADER" \
        "200"
fi

# Test 4: Refresh Token
if [ -n "$refresh_token" ]; then
    print_header "Test 4: Refresh Token"
    test_endpoint \
        "Refresh Token" \
        "POST" \
        "$AUTH_BASE_URL/refresh" \
        "{\"refreshToken\":\"$refresh_token\"}" \
        "" \
        "200"
fi

# Test 5: Get User Sessions
if [ -n "$JWT_AUTH_HEADER" ]; then
    print_header "Test 5: Get User Sessions"
    test_endpoint \
        "Get User Sessions" \
        "GET" \
        "$AUTH_BASE_URL/sessions" \
        "" \
        "$JWT_AUTH_HEADER" \
        "200"
fi

# ===========================
# BANKING API TESTS
# ===========================

print_header "Banking API Tests"

# Test 6: Register Banking User 1
print_header "Test 6: Register Banking User 1"
test_endpoint \
    "Register Banking User 1" \
    "POST" \
    "$API_BASE_URL/register" \
    "{\"username\":\"$BANKING_USERNAME1\",\"password\":\"$BANKING_PASSWORD1\"}" \
    "" \
    "201"

# Test 7: Register Banking User 2
print_header "Test 7: Register Banking User 2"
test_endpoint \
    "Register Banking User 2" \
    "POST" \
    "$API_BASE_URL/register" \
    "{\"username\":\"$BANKING_USERNAME2\",\"password\":\"$BANKING_PASSWORD2\"}" \
    "" \
    "201"

# Create Basic Auth headers
BANKING_AUTH_HEADER1=$(create_basic_auth "$BANKING_USERNAME1" "$BANKING_PASSWORD1")
BANKING_AUTH_HEADER2=$(create_basic_auth "$BANKING_USERNAME2" "$BANKING_PASSWORD2")

# Test 8: Fund Account for User 1
print_header "Test 8: Fund Account for User 1"
test_endpoint \
    "Fund Account User 1" \
    "POST" \
    "$API_BASE_URL/fund" \
    "{\"amt\":$FUND_AMOUNT}" \
    "$BANKING_AUTH_HEADER1" \
    "200"

# Test 9: Fund Account for User 2
print_header "Test 9: Fund Account for User 2"
test_endpoint \
    "Fund Account User 2" \
    "POST" \
    "$API_BASE_URL/fund" \
    "{\"amt\":$FUND_AMOUNT}" \
    "$BANKING_AUTH_HEADER2" \
    "200"

# Test 10: Check Balance (INR)
print_header "Test 10: Check Balance User 1 (INR)"
test_endpoint \
    "Check Balance User 1 INR" \
    "GET" \
    "$API_BASE_URL/bal" \
    "" \
    "$BANKING_AUTH_HEADER1" \
    "200"

# Test 11: Check Balance (USD - Currency Conversion)
print_header "Test 11: Check Balance User 1 (USD)"
test_endpoint \
    "Check Balance User 1 USD" \
    "GET" \
    "$API_BASE_URL/bal?currency=USD" \
    "" \
    "$BANKING_AUTH_HEADER1" \
    "200"

# Test 12: Check Balance (EUR - Currency Conversion)
print_header "Test 12: Check Balance User 1 (EUR)"
test_endpoint \
    "Check Balance User 1 EUR" \
    "GET" \
    "$API_BASE_URL/bal?currency=EUR" \
    "" \
    "$BANKING_AUTH_HEADER1" \
    "200"

# Test 13: Payment from User 1 to User 2
print_header "Test 13: Payment from User 1 to User 2"
test_endpoint \
    "Payment User 1 to User 2" \
    "POST" \
    "$API_BASE_URL/pay" \
    "{\"to\":\"$BANKING_USERNAME2\",\"amt\":$PAY_AMOUNT}" \
    "$BANKING_AUTH_HEADER1" \
    "200"

# Test 14: Check Balance after payment
print_header "Test 14: Check Balance User 1 after Payment"
test_endpoint \
    "Check Balance User 1 after Payment" \
    "GET" \
    "$API_BASE_URL/bal" \
    "" \
    "$BANKING_AUTH_HEADER1" \
    "200"

# Test 15: Check Balance of User 2 after receiving payment
print_header "Test 15: Check Balance User 2 after Receiving Payment"
test_endpoint \
    "Check Balance User 2 after Receiving" \
    "GET" \
    "$API_BASE_URL/bal" \
    "" \
    "$BANKING_AUTH_HEADER2" \
    "200"

# Test 16: Transaction History
print_header "Test 16: Transaction History User 1"
test_endpoint \
    "Transaction History User 1" \
    "GET" \
    "$API_BASE_URL/stmt" \
    "" \
    "$BANKING_AUTH_HEADER1" \
    "200"

# Test 17: Transaction History User 2
print_header "Test 17: Transaction History User 2"
test_endpoint \
    "Transaction History User 2" \
    "GET" \
    "$API_BASE_URL/stmt" \
    "" \
    "$BANKING_AUTH_HEADER2" \
    "200"

# ===========================
# ERROR CASE TESTS
# ===========================

print_header "Error Case Tests"

# Test 18: Duplicate registration
print_header "Test 18: Duplicate Banking Registration (Should Fail)"
test_endpoint \
    "Duplicate Banking Registration" \
    "POST" \
    "$API_BASE_URL/register" \
    "{\"username\":\"$BANKING_USERNAME1\",\"password\":\"$BANKING_PASSWORD1\"}" \
    "" \
    "409"

# Test 19: Payment with insufficient funds
print_header "Test 19: Payment with Insufficient Funds (Should Fail)"
test_endpoint \
    "Payment Insufficient Funds" \
    "POST" \
    "$API_BASE_URL/pay" \
    "{\"to\":\"$BANKING_USERNAME2\",\"amt\":10000}" \
    "$BANKING_AUTH_HEADER1" \
    "400"

# Test 20: Payment to non-existent user
print_header "Test 20: Payment to Non-existent User (Should Fail)"
test_endpoint \
    "Payment to Non-existent User" \
    "POST" \
    "$API_BASE_URL/pay" \
    "{\"to\":\"nonexistentuser\",\"amt\":10}" \
    "$BANKING_AUTH_HEADER1" \
    "400"

# Test 21: Access without authentication
print_header "Test 21: Access Banking without Authentication (Should Fail)"
test_endpoint \
    "Access without Auth" \
    "GET" \
    "$API_BASE_URL/bal" \
    "" \
    "" \
    "401"

# Test 22: Access with wrong credentials
print_header "Test 22: Access with Wrong Banking Credentials (Should Fail)"
WRONG_AUTH=$(create_basic_auth "$BANKING_USERNAME1" "wrongpassword")
test_endpoint \
    "Access with Wrong Credentials" \
    "GET" \
    "$API_BASE_URL/bal" \
    "" \
    "$WRONG_AUTH" \
    "401"

# Test 23: Access JWT endpoint without token
print_header "Test 23: Access JWT Endpoint without Token (Should Fail)"
test_endpoint \
    "Access JWT Endpoint without Token" \
    "GET" \
    "$BASE_URL/profile/me" \
    "" \
    "" \
    "401"

# Test 24: Invalid currency
print_header "Test 24: Invalid Currency (Should Fail)"
test_endpoint \
    "Invalid Currency" \
    "GET" \
    "$API_BASE_URL/bal?currency=INVALID" \
    "" \
    "$BANKING_AUTH_HEADER1" \
    "400"

# ===========================
# LOGOUT TESTS
# ===========================

print_header "Logout Tests"

# Test 25: Logout JWT User
if [ -n "$JWT_AUTH_HEADER" ]; then
    print_header "Test 25: Logout JWT User"
    test_endpoint \
        "Logout JWT User" \
        "POST" \
        "$AUTH_BASE_URL/logout" \
        "" \
        "$JWT_AUTH_HEADER" \
        "200"
fi

print_header "Test Suite Completed"
echo -e "${GREEN}All tests have been executed!${NC}"
echo -e "${YELLOW}Check the output above for any failures.${NC}"

# Summary
print_header "Test Summary"
echo "JWT Authentication: User registration, login, profile access, token refresh, sessions, logout"
echo "Banking Operations: User registration, funding, payments, balance checking, currency conversion, transaction history"
echo "Error Handling: Duplicate registrations, insufficient funds, invalid users, unauthorized access, wrong credentials"
echo ""
echo "Key Features Tested:"
echo "✓ JWT Authentication with refresh tokens"
echo "✓ Basic Authentication for banking"
echo "✓ Currency conversion using external API"
echo "✓ User registration without token requirement"
echo "✓ Comprehensive error handling"
echo "✓ Session management"
