package com.nam.controller;

import com.nam.exception.TokenRefreshException;
import com.nam.exception.UserException;
import com.nam.model.ERole;
import com.nam.model.RefreshToken;
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
import com.nam.security.jwt.JwtProvider;
import com.nam.security.services.RefreshTokenService;
import com.nam.security.services.UserDetailsImpl;
import com.nam.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthController authController;

    private SignupStudentRequest signupStudentRequest;
    private SignupTeacherRequest signupTeacherRequest;
    private LoginRequest loginRequest;
    private TokenRefreshRequest tokenRefreshRequest;
    private Student student;
    private Teacher teacher;
    private User user;
    private RefreshToken refreshToken;
    private UserDetailsImpl userDetails;
    private Set<Role> roles;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Setup roles
        roles = Set.of(new Role(1L, ERole.ROLE_STUDENT));

        // Setup student request
        signupStudentRequest = SignupStudentRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .studentId("STU001")
                .studentClass("Class A")
                .build();

        // Setup teacher request
        signupTeacherRequest = SignupTeacherRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password("password123")
                .build();

        // Setup login request
        loginRequest = new LoginRequest("john.doe@example.com", "password123");

        // Setup token refresh request
        tokenRefreshRequest = new TokenRefreshRequest();
        tokenRefreshRequest.setRefreshToken("refresh-token-123");

        // Setup user
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .roles(roles)
                .build();

        // Setup student
        student = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .studentId("STU001")
                .studentClass("Class A")
                .roles(roles)
                .build();

        // Setup teacher
        teacher = Teacher.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password("encodedPassword")
                .roles(Set.of(new Role(2L, ERole.ROLE_TEACHER)))
                .build();

        // Setup refresh token
        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("refresh-token-123");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(86400));

        // Setup user details
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
        userDetails = new UserDetailsImpl(
                1L,
                "john.doe@example.com",
                "john.doe@example.com",
                "encodedPassword",
                authorities
        );
    }

    @DisplayName("JUnit test for createStudent method - success")
    @Test
    void givenSignupStudentRequest_whenCreateStudent_thenReturnApiResponse() throws UserException {
        // given - precondition or setup
        given(userService.createStudent(signupStudentRequest)).willReturn(student);

        // when - action or the behaviour that we are going test
        ResponseEntity<ApiResponse> response = authController.createStudent(signupStudentRequest);

        // then - verify the output
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Student registered successfully");
        assertThat(response.getBody().isStatus()).isTrue();
        verify(userService, times(1)).createStudent(signupStudentRequest);
    }

    @DisplayName("JUnit test for createStudent method - throws UserException")
    @Test
    void givenSignupStudentRequest_whenCreateStudent_thenThrowUserException() throws UserException {
        // given - precondition or setup
        given(userService.createStudent(signupStudentRequest))
                .willThrow(new UserException("User already exists with email: " + signupStudentRequest.getEmail()));

        // when - action or the behaviour that we are going to test
        assertThrows(UserException.class, () -> authController.createStudent(signupStudentRequest));

        // then - verify the output
        verify(userService, times(1)).createStudent(signupStudentRequest);
    }

    @DisplayName("JUnit test for createTeacher method - success")
    @Test
    void givenSignupTeacherRequest_whenCreateTeacher_thenReturnApiResponse() throws UserException {
        // given - precondition or setup
        given(userService.createTeacher(signupTeacherRequest)).willReturn(teacher);

        // when - action or the behaviour that we are going test
        ResponseEntity<ApiResponse> response = authController.createTeacher(signupTeacherRequest);

        // then - verify the output
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Teacher registered successfully");
        assertThat(response.getBody().isStatus()).isTrue();
        verify(userService, times(1)).createTeacher(signupTeacherRequest);
    }

    @DisplayName("JUnit test for createTeacher method - throws UserException")
    @Test
    void givenSignupTeacherRequest_whenCreateTeacher_thenThrowUserException() throws UserException {
        // given - precondition or setup
        given(userService.createTeacher(signupTeacherRequest))
                .willThrow(new UserException("User already exists with email: " + signupTeacherRequest.getEmail()));

        // when - action or the behaviour that we are going to test
        assertThrows(UserException.class, () -> authController.createTeacher(signupTeacherRequest));

        // then - verify the output
        verify(userService, times(1)).createTeacher(signupTeacherRequest);
    }

    @DisplayName("JUnit test for authenticateUser method - success")
    @Test
    void givenLoginRequest_whenAuthenticateUser_thenReturnJwtResponse() {
        // given - precondition or setup
        String jwtToken = "jwt-access-token-123";
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(jwtProvider.generateToken(authentication)).willReturn(jwtToken);
        given(refreshTokenService.createRefreshToken(userDetails.getId())).willReturn(refreshToken);

        // when - action or the behaviour that we are going test
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // then - verify the output
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(JwtResponse.class);
        
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertThat(jwtResponse.getAccessToken()).isEqualTo(jwtToken);
        assertThat(jwtResponse.getRefreshToken()).isEqualTo(refreshToken.getToken());
        assertThat(jwtResponse.getId()).isEqualTo(userDetails.getId());
        assertThat(jwtResponse.getEmail()).isEqualTo(userDetails.getEmail());
        assertThat(jwtResponse.getRoles()).isNotEmpty();
        assertThat(jwtResponse.getRoles().get(0)).isEqualTo("ROLE_STUDENT");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtProvider, times(1)).generateToken(authentication);
        verify(refreshTokenService, times(1)).createRefreshToken(userDetails.getId());
    }

    @DisplayName("JUnit test for authenticateUser method - throws BadCredentialsException")
    @Test
    void givenInvalidLoginRequest_whenAuthenticateUser_thenThrowBadCredentialsException() {
        // given - precondition or setup
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Invalid credentials"));

        // when - action or the behaviour that we are going to test
        assertThrows(BadCredentialsException.class, () -> authController.authenticateUser(loginRequest));

        // then - verify the output
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtProvider, never()).generateToken(any());
        verify(refreshTokenService, never()).createRefreshToken(anyLong());
    }

    @DisplayName("JUnit test for refreshtoken method - success")
    @Test
    void givenTokenRefreshRequest_whenRefreshtoken_thenReturnTokenRefreshResponse() {
        // given - precondition or setup
        String newJwtToken = "new-jwt-access-token-123";
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("new-refresh-token-456");
        newRefreshToken.setUser(user);

        given(refreshTokenService.findByToken(tokenRefreshRequest.getRefreshToken()))
                .willReturn(Optional.of(refreshToken));
        given(refreshTokenService.verifyExpiration(refreshToken)).willReturn(refreshToken);
        given(jwtProvider.generateTokenByEmail(user.getEmail())).willReturn(newJwtToken);
        given(refreshTokenService.createRefreshToken(user.getId())).willReturn(newRefreshToken);

        // when - action or the behaviour that we are going test
        ResponseEntity<?> response = authController.refreshtoken(tokenRefreshRequest);

        // then - verify the output
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(TokenRefreshResponse.class);

        TokenRefreshResponse tokenRefreshResponse = (TokenRefreshResponse) response.getBody();
        assertThat(tokenRefreshResponse.getAccessToken()).isEqualTo(newJwtToken);
        assertThat(tokenRefreshResponse.getRefreshToken()).isEqualTo(newRefreshToken.getToken());
        assertThat(tokenRefreshResponse.getTokenType()).isEqualTo("Bearer");

        verify(refreshTokenService, times(1)).findByToken(tokenRefreshRequest.getRefreshToken());
        verify(refreshTokenService, times(1)).verifyExpiration(refreshToken);
        verify(jwtProvider, times(1)).generateTokenByEmail(user.getEmail());
        verify(refreshTokenService, times(1)).createRefreshToken(user.getId());
    }

    @DisplayName("JUnit test for refreshtoken method - token not found")
    @Test
    void givenInvalidTokenRefreshRequest_whenRefreshtoken_thenThrowTokenRefreshException() {
        // given - precondition or setup
        given(refreshTokenService.findByToken(tokenRefreshRequest.getRefreshToken()))
                .willReturn(Optional.empty());

        // when - action or the behaviour that we are going to test
        assertThrows(TokenRefreshException.class, () -> authController.refreshtoken(tokenRefreshRequest));

        // then - verify the output
        verify(refreshTokenService, times(1)).findByToken(tokenRefreshRequest.getRefreshToken());
        verify(refreshTokenService, never()).verifyExpiration(any());
        verify(jwtProvider, never()).generateTokenByEmail(anyString());
        verify(refreshTokenService, never()).createRefreshToken(anyLong());
    }

    @DisplayName("JUnit test for refreshtoken method - expired token")
    @Test
    void givenExpiredTokenRefreshRequest_whenRefreshtoken_thenThrowTokenRefreshException() {
        // given - precondition or setup
        given(refreshTokenService.findByToken(tokenRefreshRequest.getRefreshToken()))
                .willReturn(Optional.of(refreshToken));
        given(refreshTokenService.verifyExpiration(refreshToken))
                .willThrow(new TokenRefreshException(refreshToken.getToken(), "Refresh token was expired"));

        // when - action or the behaviour that we are going to test
        assertThrows(TokenRefreshException.class, () -> authController.refreshtoken(tokenRefreshRequest));

        // then - verify the output
        verify(refreshTokenService, times(1)).findByToken(tokenRefreshRequest.getRefreshToken());
        verify(refreshTokenService, times(1)).verifyExpiration(refreshToken);
        verify(jwtProvider, never()).generateTokenByEmail(anyString());
        verify(refreshTokenService, never()).createRefreshToken(anyLong());
    }

    @DisplayName("JUnit test for signout method - success")
    @Test
    void givenAuthenticatedUser_whenSignout_thenReturnApiResponse() {
        // given - precondition or setup
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(refreshTokenService.deleteByUserId(userDetails.getId())).willReturn(1);

        // when - action or the behaviour that we are going test
        ResponseEntity<?> response = authController.logoutUser();

        // then - verify the output
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(ApiResponse.class);

        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertThat(apiResponse.getMessage()).isEqualTo("User logged out successfully");
        assertThat(apiResponse.isStatus()).isTrue();

        verify(refreshTokenService, times(1)).deleteByUserId(userDetails.getId());
    }

    @DisplayName("JUnit test for signout method - no authentication")
    @Test
    void givenNoAuthentication_whenSignout_thenReturnApiResponse() {
        // given - precondition or setup
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(null);

        // when - action or the behaviour that we are going test
        ResponseEntity<?> response = authController.logoutUser();

        // then - verify the output
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(ApiResponse.class);

        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertThat(apiResponse.getMessage()).isEqualTo("User logged out successfully");
        assertThat(apiResponse.isStatus()).isTrue();

        verify(refreshTokenService, never()).deleteByUserId(anyLong());
    }

    @DisplayName("JUnit test for signout method - invalid principal type")
    @Test
    void givenInvalidPrincipalType_whenSignout_thenReturnApiResponse() {
        // given - precondition or setup
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn("invalid-principal");

        // when - action or the behaviour that we are going test
        ResponseEntity<?> response = authController.logoutUser();

        // then - verify the output
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(ApiResponse.class);

        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertThat(apiResponse.getMessage()).isEqualTo("User logged out successfully");
        assertThat(apiResponse.isStatus()).isTrue();

        verify(refreshTokenService, never()).deleteByUserId(anyLong());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
