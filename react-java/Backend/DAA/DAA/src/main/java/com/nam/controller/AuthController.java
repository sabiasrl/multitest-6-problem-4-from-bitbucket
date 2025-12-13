package com.nam.controller;

import com.nam.exception.TokenRefreshException;
import com.nam.exception.UserException;
import com.nam.model.RefreshToken;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup/student")
    public ResponseEntity<ApiResponse> createStudent(@RequestBody SignupStudentRequest request) throws UserException {
        userService.createStudent(request);
        ApiResponse response = ApiResponse.builder()
                .message("Student registered successfully")
                .status(true)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signup/teacher")
    public ResponseEntity<ApiResponse> createTeacher(@RequestBody SignupTeacherRequest request) throws UserException {
        userService.createTeacher(request);
        ApiResponse response = ApiResponse.builder()
                .message("Teacher registered successfully")
                .status(true)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getEmail(),
                roles
        );

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtProvider.generateTokenByEmail(user.getEmail());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            refreshTokenService.deleteByUserId(userDetails.getId());
        }
        ApiResponse response = ApiResponse.builder()
                .message("User logged out successfully")
                .status(true)
                .build();
        return ResponseEntity.ok(response);
    }

}