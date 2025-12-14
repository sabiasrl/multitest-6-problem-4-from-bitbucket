package com.nam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nam.model.ERole;
import com.nam.model.Role;
import com.nam.model.Student;
import com.nam.model.Teacher;
import com.nam.model.User;
import com.nam.payload.request.LoginRequest;
import com.nam.payload.request.SignupStudentRequest;
import com.nam.payload.request.SignupTeacherRequest;
import com.nam.payload.request.TokenRefreshRequest;
import com.nam.payload.response.ApiResponse;
import com.nam.payload.response.JwtResponse;
import com.nam.payload.response.TokenRefreshResponse;
import com.nam.repository.RefreshTokenRepository;
import com.nam.repository.RoleRepository;
import com.nam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Boot integration test for AuthController.
 * 
 * This test uses @SpringBootTest with H2 in-memory database to test
 * the actual authentication endpoints with real database operations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:auth_controller_testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
    "spring.jpa.show-sql=false"
})
@Transactional
@DisplayName("AuthController Spring Boot Integration Test")
class AuthControllerSpringBootTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role studentRole;
    private Role teacherRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Clear repositories
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Initialize roles
        studentRole = new Role();
        studentRole.setName(ERole.ROLE_STUDENT);
        studentRole = roleRepository.save(studentRole);

        teacherRole = new Role();
        teacherRole.setName(ERole.ROLE_TEACHER);
        teacherRole = roleRepository.save(teacherRole);

        adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        adminRole = roleRepository.save(adminRole);
    }

    @Test
    @DisplayName("POST /auth/signup/student - Should create student successfully")
    void givenSignupStudentRequest_whenCreateStudent_thenReturn201() throws Exception {
        // given
        SignupStudentRequest request = SignupStudentRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .studentId("STU001")
                .studentClass("Class A")
                .build();

        // when & then
        MvcResult result = mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response
        String responseBody = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("Student registered successfully");
        assertThat(response.isStatus()).isTrue();

        // Verify student was saved in database
        User savedUser = userRepository.findByEmail("john.doe@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser).isInstanceOf(Student.class);
        Student savedStudent = (Student) savedUser;
        assertThat(savedStudent.getStudentId()).isEqualTo("STU001");
        assertThat(savedStudent.getStudentClass()).isEqualTo("Class A");
        assertThat(savedStudent.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("POST /auth/signup/student - Should return error when email already exists")
    void givenExistingEmail_whenCreateStudent_thenReturnError() throws Exception {
        // given - create existing student
        Student existingStudent = Student.builder()
                .firstName("Existing")
                .lastName("Student")
                .email("existing@example.com")
                .password(passwordEncoder.encode("password"))
                .studentId("STU000")
                .studentClass("Class X")
                .roles(java.util.Set.of(studentRole))
                .build();
        userRepository.save(existingStudent);

        SignupStudentRequest request = SignupStudentRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("existing@example.com") // Same email
                .password("password123")
                .studentId("STU001")
                .studentClass("Class A")
                .build();

        // when & then
        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/signup/teacher - Should create teacher successfully")
    void givenSignupTeacherRequest_whenCreateTeacher_thenReturn201() throws Exception {
        // given
        SignupTeacherRequest request = SignupTeacherRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password("password123")
                .build();

        // when & then
        MvcResult result = mockMvc.perform(post("/auth/signup/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response
        String responseBody = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getMessage()).isEqualTo("Teacher registered successfully");
        assertThat(response.isStatus()).isTrue();

        // Verify teacher was saved in database
        User savedUser = userRepository.findByEmail("jane.smith@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser).isInstanceOf(Teacher.class);
        Teacher savedTeacher = (Teacher) savedUser;
        assertThat(savedTeacher.getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("POST /auth/signin - Should authenticate user and return JWT tokens")
    void givenValidCredentials_whenSignin_thenReturnJwtResponse() throws Exception {
        // given - create a student user
        Student student = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("password123"))
                .studentId("STU001")
                .studentClass("Class A")
                .roles(java.util.Set.of(studentRole))
                .build();
        userRepository.save(student);

        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "password123");

        // when & then
        MvcResult result = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response
        String responseBody = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseBody, JwtResponse.class);
        assertThat(jwtResponse.getAccessToken()).isNotNull();
        assertThat(jwtResponse.getRefreshToken()).isNotNull();
        assertThat(jwtResponse.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(jwtResponse.getRoles()).isNotEmpty();
        assertThat(jwtResponse.getRoles()).contains("ROLE_STUDENT");
    }

    @Test
    @DisplayName("POST /auth/signin - Should return error for invalid credentials")
    void givenInvalidCredentials_whenSignin_thenReturnError() throws Exception {
        // given - create a student user
        Student student = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("password123"))
                .studentId("STU001")
                .studentClass("Class A")
                .roles(java.util.Set.of(studentRole))
                .build();
        userRepository.save(student);

        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "wrongpassword");

        // when & then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/refreshtoken - Should refresh token successfully")
    void givenValidRefreshToken_whenRefreshtoken_thenReturnNewTokens() throws Exception {
        // given - create a student and sign in to get tokens
        Student student = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("password123"))
                .studentId("STU001")
                .studentClass("Class A")
                .roles(java.util.Set.of(studentRole))
                .build();
        student = (Student) userRepository.save(student);

        // Sign in to get refresh token
        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "password123");
        MvcResult signinResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse signinResponse = objectMapper.readValue(
                signinResult.getResponse().getContentAsString(), JwtResponse.class);
        String refreshToken = signinResponse.getRefreshToken();

        // when - refresh token
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest();
        tokenRefreshRequest.setRefreshToken(refreshToken);

        // then
        MvcResult result = mockMvc.perform(post("/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response
        String responseBody = result.getResponse().getContentAsString();
        TokenRefreshResponse tokenRefreshResponse = objectMapper.readValue(
                responseBody, TokenRefreshResponse.class);
        assertThat(tokenRefreshResponse.getAccessToken()).isNotNull();
        assertThat(tokenRefreshResponse.getRefreshToken()).isNotNull();
        assertThat(tokenRefreshResponse.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("POST /auth/refreshtoken - Should return error for invalid refresh token")
    void givenInvalidRefreshToken_whenRefreshtoken_thenReturnError() throws Exception {
        // given
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest();
        tokenRefreshRequest.setRefreshToken("invalid-refresh-token");

        // when & then
        mockMvc.perform(post("/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /auth/signout - Should logout user successfully")
    void givenAuthenticatedUser_whenSignout_thenReturnSuccess() throws Exception {
        // given - create a student and sign in
        Student student = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("password123"))
                .studentId("STU001")
                .studentClass("Class A")
                .roles(java.util.Set.of(studentRole))
                .build();
        student = (Student) userRepository.save(student);

        // Sign in to create refresh token
        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "password123");
        MvcResult signinResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse signinResponse = objectMapper.readValue(
                signinResult.getResponse().getContentAsString(), JwtResponse.class);
        String accessToken = signinResponse.getAccessToken();

        // Verify refresh token exists before logout
        assertThat(refreshTokenRepository.findByToken(signinResponse.getRefreshToken())).isPresent();

        // when - logout (note: signout endpoint doesn't require authentication in this implementation)
        MvcResult result = mockMvc.perform(post("/auth/signout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response
        String responseBody = result.getResponse().getContentAsString();
        ApiResponse apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        assertThat(apiResponse.getMessage()).isEqualTo("User logged out successfully");
        assertThat(apiResponse.isStatus()).isTrue();
    }

    @Test
    @DisplayName("POST /auth/signout - Should handle logout without authentication gracefully")
    void givenNoAuthentication_whenSignout_thenReturnSuccess() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(post("/auth/signout"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify response
        String responseBody = result.getResponse().getContentAsString();
        ApiResponse apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        assertThat(apiResponse.getMessage()).isEqualTo("User logged out successfully");
        assertThat(apiResponse.isStatus()).isTrue();
    }
}
