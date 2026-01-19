# SEC App API

A comprehensive Spring Boot application with JWT authentication and banking operations.

## Issues Fixed

### 1. ✅ Banking Registration Token Issue

**Problem**: Banking registration endpoint was asking for authentication token.
**Solution**:

- Added `/api/v1/register` to excluded patterns in interceptors (`WebMvcConfig.java`)
- Configured security to permit banking registration without authentication

### 2. ✅ Email vs Username Login Inconsistency

**Problem**: Banking registration uses username but login expects email.
**Solution**:

- Banking users register with username and the system stores it as email for Basic Auth compatibility
- Added `CustomUserDetailsService` for Basic Authentication support
- For banking operations, users can use their username with Basic Authentication

### 3. ✅ Currency Conversion Service

**Problem**: Already correctly implemented using external API.
**Status**: ✅ Working - Uses `https://open.er-api.com/v6/latest/USD` for real-time currency conversion

### 4. ✅ DynamoDB Configuration

**Problem**: Missing User table bean causing application startup failure.
**Solution**: Added `userTable` bean in `DynamoDbConfig.java`

## Authentication Systems

The application supports two authentication systems:

### 1. JWT Authentication (for general app features)

- **Registration**: `/auth/register` - Register with name and password (email optional)
- **Login**: `/auth/login` - Login with email and password
- **Profile**: `/profile/me` - Get user profile (requires JWT token)
- **Sessions**: `/auth/sessions` - Manage user sessions
- **Logout**: `/auth/logout` - Logout and invalidate tokens

### 2. Basic Authentication (for banking operations)

- **Registration**: `/api/v1/register` - Register with username and password
- **Banking Operations**: All banking endpoints use HTTP Basic Authentication

## API Endpoints

### Authentication Endpoints

```
POST /auth/register          - Register JWT user
POST /auth/login             - Login JWT user
POST /auth/logout            - Logout JWT user
POST /auth/refresh           - Refresh JWT token
GET  /auth/sessions          - Get user sessions
DELETE /auth/sessions/{id}   - Kill specific session
```

### Banking Endpoints (Basic Auth Required)

```
POST /api/v1/register        - Register banking user
POST /api/v1/fund           - Fund account
POST /api/v1/pay            - Pay another user
GET  /api/v1/bal            - Check balance (supports currency conversion)
GET  /api/v1/stmt           - Get transaction history
```

### Profile Endpoints (JWT Required)

```
GET /profile/me             - Get user profile
```

## Currency Conversion

The `/api/v1/bal` endpoint supports currency conversion:

- `GET /api/v1/bal` - Balance in INR (default)
- `GET /api/v1/bal?currency=USD` - Balance in USD
- `GET /api/v1/bal?currency=EUR` - Balance in EUR
- Supports any currency code available from the external API

## Setup Instructions

### Prerequisites

- Java 21
- Maven 3.6+
- Redis (for session management)
- AWS DynamoDB (or DynamoDB Local for development)

### Local Development Setup

1. **Start Redis** (for session management):

   ```bash
   # Using Docker
   docker run -d -p 6379:6379 redis:latest

   # Or install locally and start
   redis-server
   ```

2. **Start DynamoDB Local** (optional for local development):

   ```bash
   # Using Docker
   docker run -p 8000:8000 amazon/dynamodb-local
   ```

3. **Configure Environment Variables** (optional):

   ```bash
   export AWS_REGION=us-east-1
   export REDIS_HOST=localhost
   export REDIS_PORT=6379
   ```

4. **Run the Application**:

   ```bash
   # Using Maven
   ./mvnw spring-boot:run

   # Or with local profile
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Testing the API

Use the provided test scripts:

1. **Complete API Test** (JWT + Banking):

   ```bash
   ./test-complete-api.sh http://localhost:8080
   ```

2. **Banking API Only**:
   ```bash
   ./test-banking-api.sh http://localhost:8080/api/v1
   ```

### Example Usage

#### Banking Operations

```bash
# 1. Register banking user
curl -X POST http://localhost:8080/api/v1/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}'

# 2. Fund account (Basic Auth)
curl -X POST http://localhost:8080/api/v1/fund \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'alice:password123' | base64)" \
  -d '{"amt":1000}'

# 3. Check balance in USD
curl -X GET "http://localhost:8080/api/v1/bal?currency=USD" \
  -H "Authorization: Basic $(echo -n 'alice:password123' | base64)"

# 4. Pay another user
curl -X POST http://localhost:8080/api/v1/pay \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'alice:password123' | base64)" \
  -d '{"to":"bob","amt":100}'
```

#### JWT Operations

```bash
# 1. Register JWT user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","password":"StrongP@ss123"}'

# 2. Login to get token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"johndoe@example.com","password":"StrongP@ss123"}'

# 3. Use token to access profile
curl -X GET http://localhost:8080/profile/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Key Features

- ✅ **Dual Authentication**: JWT for app features, Basic Auth for banking
- ✅ **Currency Conversion**: Real-time exchange rates from external API
- ✅ **Session Management**: Redis-based session tracking with JWT
- ✅ **Comprehensive Security**: CORS, CSRF protection, secure headers
- ✅ **Banking Operations**: Account funding, payments, transaction history
- ✅ **Error Handling**: Comprehensive error responses for all scenarios
- ✅ **Testing**: Complete test scripts for all endpoints

## Architecture

- **Framework**: Spring Boot 3.5.0 with Java 21
- **Security**: Spring Security with JWT and Basic Authentication
- **Database**: AWS DynamoDB with Enhanced Client
- **Cache/Sessions**: Redis for session management
- **External API**: Currency conversion via open exchange rates API
- **Documentation**: Comprehensive API documentation with examples

## Troubleshooting

### Common Issues

1. **DynamoDB Connection Issues**:

   - For local development, the app can run without DynamoDB if you're just testing the architecture
   - For production, ensure AWS credentials are properly configured

2. **Redis Connection Issues**:

   - Ensure Redis is running on the configured host/port
   - Check Redis connectivity with `redis-cli ping`

3. **Authentication Issues**:

   - Banking endpoints use Basic Auth with username:password
   - JWT endpoints use Bearer tokens
   - Ensure you're using the correct authentication method for each endpoint

4. **Currency Conversion Issues**:
   - Requires internet connectivity to fetch exchange rates
   - Falls back gracefully if external API is unavailable
