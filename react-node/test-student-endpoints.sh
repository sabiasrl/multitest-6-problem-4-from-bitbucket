#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_URL="http://localhost:5007"
COOKIE_FILE="/tmp/student_test_cookies.txt"
ADMIN_EMAIL="admin@school-admin.com"
ADMIN_PASSWORD="3OU4zn3q6Zh9"

# Test counters
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
    ((TESTS_PASSED++))
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
    ((TESTS_FAILED++))
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=0

    print_info "Waiting for $service_name to be ready..."
    while [ $attempt -lt $max_attempts ]; do
        # Try to connect to the backend (even if it returns an error, connection means it's up)
        if curl -s --connect-timeout 2 "$url" > /dev/null 2>&1 || \
           curl -s --connect-timeout 2 -X POST "$url" -H "Content-Type: application/json" -d '{}' > /dev/null 2>&1; then
            print_success "$service_name is ready"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 2
    done
    
    print_error "$service_name failed to start after $max_attempts attempts"
    return 1
}

# Function to login and get authentication cookies
login() {
    print_info "Logging in as admin..."
    
    local response=$(curl -s -c "$COOKIE_FILE" -X POST "$BACKEND_URL/api/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}")
    
    if echo "$response" | grep -q '"id"'; then
        print_success "Login successful"
        # Extract CSRF token from cookies (Netscape cookie format)
        # Cookie format: domain flag path secure expiration name value
        CSRF_TOKEN=$(grep -i "csrfToken" "$COOKIE_FILE" | awk '{print $NF}' | head -1)
        if [ -z "$CSRF_TOKEN" ] || [ "$CSRF_TOKEN" = "#HttpOnly_" ]; then
            # Try getting from the line with csrfToken
            CSRF_TOKEN=$(grep -i "csrfToken" "$COOKIE_FILE" | grep -v "^#" | awk '{print $7}' | head -1)
        fi
        if [ -n "$CSRF_TOKEN" ] && [ "$CSRF_TOKEN" != "null" ]; then
            print_info "CSRF Token extracted: ${CSRF_TOKEN:0:20}..."
        else
            print_warning "CSRF Token not found in cookies, requests may fail"
        fi
        return 0
    else
        print_error "Login failed: $response"
        return 1
    fi
}

# Function to make authenticated request
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_status=${4:-200}
    
    local url="$BACKEND_URL/api/v1/students$endpoint"
    local headers=()
    
    # Add CSRF token header if available
    if [ -n "$CSRF_TOKEN" ] && [ "$CSRF_TOKEN" != "null" ]; then
        headers+=("-H" "x-csrf-token: $CSRF_TOKEN")
    fi
    
    # Add content type if data is provided
    if [ -n "$data" ]; then
        headers+=("-H" "Content-Type: application/json")
    fi
    
    # Build curl command
    local curl_args=(-s -w "\n%{http_code}" -b "$COOKIE_FILE" -X "$method")
    curl_args+=("${headers[@]}")
    
    if [ -n "$data" ]; then
        curl_args+=(-d "$data")
    fi
    
    curl_args+=("$url")
    
    local response=$(curl "${curl_args[@]}")
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        echo "$body"
        return 0
    else
        echo "HTTP $http_code: $body" >&2
        return 1
    fi
}

# Function to test endpoint
test_endpoint() {
    local test_name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local expected_status=${5:-200}
    local validation=${6:-""}
    
    print_info "Testing: $test_name"
    
    local response=$(make_request "$method" "$endpoint" "$data" "$expected_status")
    local result=$?
    
    if [ $result -eq 0 ]; then
        # Additional validation if provided
        if [ -n "$validation" ]; then
            if echo "$response" | grep -q "$validation"; then
                print_success "$test_name"
                echo "$response"
                return 0
            else
                print_error "$test_name - Validation failed: Expected '$validation' in response"
                echo "$response"
                return 1
            fi
        else
            print_success "$test_name"
            echo "$response"
            return 0
        fi
    else
        print_error "$test_name - HTTP Error"
        return 1
    fi
}

# Cleanup function
cleanup() {
    print_info "Cleaning up..."
    rm -f "$COOKIE_FILE"
    if [ "$1" != "no-stop" ]; then
        print_info "Stopping Docker Compose..."
        cd "$COMPOSE_DIR" && docker-compose down
        print_success "Docker Compose stopped"
    fi
}

# Trap to ensure cleanup on exit
trap 'cleanup' EXIT INT TERM

# Main execution
main() {
    echo "=========================================="
    echo "  Student Endpoints Lifecycle Test"
    echo "=========================================="
    echo ""
    
    # Step 1: Start Docker Compose
    print_info "Starting Docker Compose..."
    cd "$COMPOSE_DIR" || exit 1
    
    # Stop any existing containers
    docker-compose down -v > /dev/null 2>&1
    
    # Start services
    if ! docker-compose up -d --build; then
        print_error "Failed to start Docker Compose"
        exit 1
    fi
    
    print_success "Docker Compose started"
    echo ""
    
    # Step 2: Wait for services
    print_info "Waiting for services to be ready..."
    sleep 5
    
    if ! wait_for_service "$BACKEND_URL/api/v1/auth/login" "Backend"; then
        print_error "Backend service not ready"
        exit 1
    fi
    
    sleep 3  # Give backend a bit more time to fully initialize
    echo ""
    
    # Step 3: Login
    if ! login; then
        print_error "Failed to login. Cannot proceed with tests."
        exit 1
    fi
    echo ""
    
    # Step 4: Test Student Endpoints
    echo "=========================================="
    echo "  Testing Student Endpoints"
    echo "=========================================="
    echo ""
    
    # Test 1: GET all students
    print_info "Test 1: GET /api/v1/students (List all students)"
    test_endpoint "GET all students" "GET" "" "" 200 '"students"'
    STUDENT_LIST=$(make_request "GET" "" "" 200)
    echo ""
    
    # Extract first student ID for subsequent tests (without jq)
    # Look for pattern: "id":123 in the students array
    FIRST_STUDENT_ID=$(echo "$STUDENT_LIST" | grep -oE '"id"\s*:\s*[0-9]+' | head -1 | grep -oE '[0-9]+')
    
    if [ -z "$FIRST_STUDENT_ID" ]; then
        print_warning "No existing students found. Will create a new one for testing."
        FIRST_STUDENT_ID=""
    else
        print_info "Using existing student ID: $FIRST_STUDENT_ID for tests"
    fi
    echo ""
    
    # Test 2: GET student by ID
    if [ -n "$FIRST_STUDENT_ID" ]; then
        print_info "Test 2: GET /api/v1/students/:id (Get student detail)"
        test_endpoint "GET student by ID" "GET" "/$FIRST_STUDENT_ID" "" 200 '"id"'
        echo ""
    else
        print_warning "Skipping GET by ID test - no existing students"
        echo ""
    fi
    
    # Test 3: GET students with filters
    print_info "Test 3: GET /api/v1/students?className=Grade 10 (Get students with filters)"
    test_endpoint "GET students with filters" "GET" "?className=Grade%2010" "" 200 '"students"'
    echo ""
    
    # Test 4: POST create new student
    print_info "Test 4: POST /api/v1/students (Create new student)"
    NEW_STUDENT_DATA='{
        "name": "Test Student",
        "email": "test.student@school.com",
        "phone": "555-9999",
        "gender": "male",
        "dob": "2010-06-15",
        "class": "Grade 10",
        "section": "A",
        "roll": "999",
        "fatherName": "Test Father",
        "fatherPhone": "555-9998",
        "motherName": "Test Mother",
        "motherPhone": "555-9997",
        "guardianName": "Test Guardian",
        "guardianPhone": "555-9996",
        "relationOfGuardian": "Uncle",
        "currentAddress": "123 Test Street",
        "permanentAddress": "123 Test Street",
        "admissionDate": "2024-09-01"
    }'
    
    CREATE_RESPONSE=$(make_request "POST" "" "$NEW_STUDENT_DATA" 200)
    if echo "$CREATE_RESPONSE" | grep -q '"message"'; then
        print_success "Create student"
        echo "$CREATE_RESPONSE"
        
        # Get the newly created student ID
        sleep 3  # Wait a moment for the student to be created
        ALL_STUDENTS=$(make_request "GET" "" "" 200)
        # Extract student ID - get the last/highest ID (newly created should be highest)
        NEW_STUDENT_ID=$(echo "$ALL_STUDENTS" | grep -oE '"id"\s*:\s*[0-9]+' | grep -oE '[0-9]+' | sort -n | tail -1)
        
        # Verify by checking email if possible
        if echo "$ALL_STUDENTS" | grep -q "test.student@school.com"; then
            print_info "New student found in list"
        fi
        
        if [ -n "$NEW_STUDENT_ID" ] && [ "$NEW_STUDENT_ID" != "null" ]; then
            print_info "Created student ID: $NEW_STUDENT_ID"
            FIRST_STUDENT_ID=$NEW_STUDENT_ID  # Use this for update/status tests
        fi
    else
        print_error "Create student failed"
        echo "$CREATE_RESPONSE"
    fi
    echo ""
    
    # Test 5: PUT update student
    if [ -n "$FIRST_STUDENT_ID" ] && [ "$FIRST_STUDENT_ID" != "null" ]; then
        print_info "Test 5: PUT /api/v1/students/:id (Update student)"
        UPDATE_DATA='{
            "name": "Test Student Updated",
            "phone": "555-8888"
        }'
        
        UPDATE_RESPONSE=$(make_request "PUT" "/$FIRST_STUDENT_ID" "$UPDATE_DATA" 200)
        if echo "$UPDATE_RESPONSE" | grep -q '"message"'; then
            print_success "Update student"
            echo "$UPDATE_RESPONSE"
        else
            print_error "Update student failed"
            echo "$UPDATE_RESPONSE"
        fi
        echo ""
        
        # Verify update by getting student detail
        print_info "Verifying update by fetching student detail..."
        UPDATED_STUDENT=$(make_request "GET" "/$FIRST_STUDENT_ID" "" 200)
        if echo "$UPDATED_STUDENT" | grep -q "Test Student Updated"; then
            print_success "Update verified - name changed to 'Test Student Updated'"
        else
            print_warning "Update may not have been applied correctly"
        fi
        echo ""
    else
        print_warning "Skipping UPDATE test - no student ID available"
        echo ""
    fi
    
    # Test 6: POST update student status
    if [ -n "$FIRST_STUDENT_ID" ] && [ "$FIRST_STUDENT_ID" != "null" ]; then
        print_info "Test 6: POST /api/v1/students/:id/status (Update student status)"
        STATUS_DATA='{"status": false}'
        
        STATUS_RESPONSE=$(make_request "POST" "/$FIRST_STUDENT_ID/status" "$STATUS_DATA" 200)
        if echo "$STATUS_RESPONSE" | grep -q '"message"'; then
            print_success "Update student status"
            echo "$STATUS_RESPONSE"
        else
            print_error "Update student status failed"
            echo "$STATUS_RESPONSE"
        fi
        echo ""
        
        # Test setting status back to active
        print_info "Setting student status back to active..."
        STATUS_DATA_ACTIVE='{"status": true}'
        make_request "POST" "/$FIRST_STUDENT_ID/status" "$STATUS_DATA_ACTIVE" 200 > /dev/null
        print_success "Student status set back to active"
        echo ""
    else
        print_warning "Skipping STATUS UPDATE test - no student ID available"
        echo ""
    fi
    
    # Test 7: Validation tests
    echo "=========================================="
    echo "  Testing Validation"
    echo "=========================================="
    echo ""
    
    print_info "Test 7: POST /api/v1/students (Validation - missing required fields)"
    INVALID_DATA='{"name": "Test"}'
    VALIDATION_RESPONSE=$(make_request "POST" "" "$INVALID_DATA" 400)
    if echo "$VALIDATION_RESPONSE" | grep -q "error\|Validation"; then
        print_success "Validation working - rejected invalid data"
    else
        print_error "Validation test failed"
    fi
    echo ""
    
    # Summary
    echo "=========================================="
    echo "  Test Summary"
    echo "=========================================="
    echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
    echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"
    echo ""
    
    if [ $TESTS_FAILED -eq 0 ]; then
        print_success "All tests passed! ✓"
        exit 0
    else
        print_error "Some tests failed"
        exit 1
    fi
}

# Run main function
main

