# AuthController Implementation Notes

## Quick Reference - Test Commands

```bash
# Navigate to project directory
cd DAA/DAA

# Run AuthController tests only
mvn test -Dtest=AuthControllerTest

# Run all tests
mvn test

```

## Overview

This document describes the implementation of the `AuthController` with full CRUD operations for authentication endpoints.

## Implementation Details

### Location
- **Controller**: `DAA/DAA/src/main/java/com/nam/controller/AuthController.java`
- **Tests**: `DAA/DAA/src/test/java/com/nam/controller/AuthControllerTest.java`

### Implemented Endpoints

#### 1. **POST `/auth/signup/student`** - Create Student
- **Purpose**: Register a new student user
- **Request Body**: `SignupStudentRequest` (firstName, lastName, email, password, studentId, studentClass)
- **Response**: `ApiResponse` with success message and HTTP 201 status
- **Exception**: Throws `UserException` if user already exists

#### 2. **POST `/auth/signup/teacher`** - Create Teacher
- **Purpose**: Register a new teacher user
- **Request Body**: `SignupTeacherRequest` (firstName, lastName, email, password)
- **Response**: `ApiResponse` with success message and HTTP 201 status
- **Exception**: Throws `UserException` if user already exists

#### 3. **POST `/auth/signin`** - Authenticate User (Read)
- **Purpose**: Authenticate user and return JWT tokens
- **Request Body**: `LoginRequest` (email, password)
- **Response**: `JwtResponse` containing:
  - Access token (JWT)
  - Refresh token
  - User ID, email, username
  - User roles
- **Exception**: Throws `BadCredentialsException` for invalid credentials

#### 4. **POST `/auth/refreshtoken`** - Refresh Token (Update)
- **Purpose**: Generate new access token using refresh token
- **Request Body**: `TokenRefreshRequest` (refreshToken)
- **Response**: `TokenRefreshResponse` with new access and refresh tokens
- **Exception**: Throws `TokenRefreshException` if token is invalid or expired

#### 5. **POST `/auth/signout`** - Logout User (Delete)
- **Purpose**: Logout user by deleting refresh token
- **Response**: `ApiResponse` with success message
- **Behavior**: Gracefully handles cases with no authentication or invalid principal

## Technologies Used

- **Spring Boot 3.1.2**: Framework
- **Spring Security**: Authentication and authorization
- **JWT (jjwt 0.11.1)**: Token generation and validation
- **JPA/Hibernate**: Data persistence
- **PostgreSQL**: Database (production)
- **H2**: In-memory database (testing)
- **Lombok**: Code generation
- **JUnit 5 & Mockito**: Unit testing

## Testing

### Test Coverage

The `AuthControllerTest` includes **12 comprehensive unit tests**:

1. **createStudent Tests**:
   - ✅ Success case - returns 201 with success message
   - ✅ Exception case - throws UserException when user exists

2. **createTeacher Tests**:
   - ✅ Success case - returns 201 with success message
   - ✅ Exception case - throws UserException when user exists

3. **authenticateUser Tests**:
   - ✅ Success case - returns JWT response with tokens and user info
   - ✅ Exception case - throws BadCredentialsException for invalid credentials

4. **refreshtoken Tests**:
   - ✅ Success case - returns new tokens
   - ✅ Token not found - throws TokenRefreshException
   - ✅ Expired token - throws TokenRefreshException

5. **signout Tests**:
   - ✅ Success case - deletes refresh token and returns success
   - ✅ No authentication - gracefully handles missing auth
   - ✅ Invalid principal - gracefully handles invalid principal type

### Test Verification

```bash
# Unit tests only
cd DAA/DAA && mvn test -Dtest=AuthControllerTest

# Full test suite
cd DAA/DAA && mvn test
```

## Code Quality

- ✅ Follows Spring Boot best practices
- ✅ Proper exception handling
- ✅ Comprehensive unit test coverage
- ✅ Uses dependency injection
- ✅ Follows RESTful conventions
- ✅ Proper HTTP status codes
- ✅ Security context management

## Dependencies

All required dependencies are already configured in `pom.xml`:
- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- JWT libraries (jjwt)
- JUnit 5 and Mockito for testing

## Next Steps

1. Run the test suite to verify all tests pass
2. API documentation (Swagger/OpenAPI) if needed
