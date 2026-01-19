#!/bin/bash

# Banking API Test Script
# Tests all banking endpoints with proper authentication
# 
# Usage: ./test-banking-api.sh [BASE_URL]
# Example: ./test-banking-api.sh http://localhost:8080/api/v1

set -e

# Configuration
BASE_URL=${1:-"http://localhost:8080/api/v1"}
AUTH_BASE_URL=${BASE_URL%/api/v1}/auth

# Test data
USERNAME1="testuser1"
PASSWORD1="password123"
USERNAME2="testuser2"
PASSWORD2="password456"
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
        return 0
    else
        print_error "$name - Expected: $expected_status, Got: $http_status"
        return 1
    fi
}

# Create Basic Auth header
create_basic_auth() {
    local username="$1"
    local password="$2"
    echo "Basic $(echo -n "$username:$password" | base64)"
}

print_header "Banking API Test Suite"
echo "Base URL: $BASE_URL"
echo "Auth URL: $AUTH_BASE_URL"

# Test 1: Register User 1
print_header "Test 1: Register Banking User 1"
test_endpoint \
    "Register User 1" \
    "POST" \
    "$BASE_URL/register" \
    "{\"username\":\"$USERNAME1\",\"password\":\"$PASSWORD1\"}" \
    "" \
    "201"

# Test 2: Register User 2
print_header "Test 2: Register Banking User 2"
test_endpoint \
    "Register User 2" \
    "POST" \
    "$BASE_URL/register" \
    "{\"username\":\"$USERNAME2\",\"password\":\"$PASSWORD2\"}" \
    "" \
    "201"

# Test 3: Try to register duplicate user
print_header "Test 3: Register Duplicate User (Should Fail)"
test_endpoint \
    "Register Duplicate User" \
    "POST" \
    "$BASE_URL/register" \
    "{\"username\":\"$USERNAME1\",\"password\":\"$PASSWORD1\"}" \
    "" \
    "409"

# Test 4: Fund Account for User 1
print_header "Test 4: Fund Account for User 1"
AUTH_HEADER1=$(create_basic_auth "$USERNAME1" "$PASSWORD1")
test_endpoint \
    "Fund Account User 1" \
    "POST" \
    "$BASE_URL/fund" \
    "{\"amt\":$FUND_AMOUNT}" \
    "$AUTH_HEADER1" \
    "200"

# Test 5: Fund Account for User 2
print_header "Test 5: Fund Account for User 2"
AUTH_HEADER2=$(create_basic_auth "$USERNAME2" "$PASSWORD2")
test_endpoint \
    "Fund Account User 2" \
    "POST" \
    "$BASE_URL/fund" \
    "{\"amt\":$FUND_AMOUNT}" \
    "$AUTH_HEADER2" \
    "200"

# Test 6: Check Balance for User 1 (INR)
print_header "Test 6: Check Balance User 1 (INR)"
test_endpoint \
    "Check Balance User 1 INR" \
    "GET" \
    "$BASE_URL/bal" \
    "" \
    "$AUTH_HEADER1" \
    "200"

# Test 7: Check Balance for User 1 (USD)
print_header "Test 7: Check Balance User 1 (USD)"
test_endpoint \
    "Check Balance User 1 USD" \
    "GET" \
    "$BASE_URL/bal?currency=USD" \
    "" \
    "$AUTH_HEADER1" \
    "200"

# Test 8: Check Balance for User 1 (EUR)
print_header "Test 8: Check Balance User 1 (EUR)"
test_endpoint \
    "Check Balance User 1 EUR" \
    "GET" \
    "$BASE_URL/bal?currency=EUR" \
    "" \
    "$AUTH_HEADER1" \
    "200"

# Test 9: Pay from User 1 to User 2
print_header "Test 9: Payment from User 1 to User 2"
test_endpoint \
    "Payment User 1 to User 2" \
    "POST" \
    "$BASE_URL/pay" \
    "{\"to\":\"$USERNAME2\",\"amt\":$PAY_AMOUNT}" \
    "$AUTH_HEADER1" \
    "200"

# Test 10: Check Balance after payment
print_header "Test 10: Check Balance User 1 after Payment"
test_endpoint \
    "Check Balance User 1 after Payment" \
    "GET" \
    "$BASE_URL/bal" \
    "" \
    "$AUTH_HEADER1" \
    "200"

# Test 11: Check Balance of User 2 after receiving payment
print_header "Test 11: Check Balance User 2 after Receiving Payment"
test_endpoint \
    "Check Balance User 2 after Receiving" \
    "GET" \
    "$BASE_URL/bal" \
    "" \
    "$AUTH_HEADER2" \
    "200"

# Test 12: Get Transaction History for User 1
print_header "Test 12: Transaction History User 1"
test_endpoint \
    "Transaction History User 1" \
    "GET" \
    "$BASE_URL/stmt" \
    "" \
    "$AUTH_HEADER1" \
    "200"

# Test 13: Get Transaction History for User 2
print_header "Test 13: Transaction History User 2"
test_endpoint \
    "Transaction History User 2" \
    "GET" \
    "$BASE_URL/stmt" \
    "" \
    "$AUTH_HEADER2" \
    "200"

# Test 14: Try payment with insufficient funds
print_header "Test 14: Payment with Insufficient Funds (Should Fail)"
test_endpoint \
    "Payment Insufficient Funds" \
    "POST" \
    "$BASE_URL/pay" \
    "{\"to\":\"$USERNAME2\",\"amt\":10000}" \
    "$AUTH_HEADER1" \
    "400"

# Test 15: Try payment to non-existent user
print_header "Test 15: Payment to Non-existent User (Should Fail)"
test_endpoint \
    "Payment to Non-existent User" \
    "POST" \
    "$BASE_URL/pay" \
    "{\"to\":\"nonexistentuser\",\"amt\":10}" \
    "$AUTH_HEADER1" \
    "400"

# Test 16: Try accessing without authentication
print_header "Test 16: Access without Authentication (Should Fail)"
test_endpoint \
    "Access without Auth" \
    "GET" \
    "$BASE_URL/bal" \
    "" \
    "" \
    "401"

# Test 17: Try accessing with wrong credentials
print_header "Test 17: Access with Wrong Credentials (Should Fail)"
WRONG_AUTH=$(create_basic_auth "$USERNAME1" "wrongpassword")
test_endpoint \
    "Access with Wrong Credentials" \
    "GET" \
    "$BASE_URL/bal" \
    "" \
    "$WRONG_AUTH" \
    "401"

print_header "Test Suite Completed"
echo -e "${GREEN}All tests have been executed!${NC}"
echo -e "${YELLOW}Check the output above for any failures.${NC}"
