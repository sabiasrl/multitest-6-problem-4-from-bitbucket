package com.nam;

import com.nam.repository.*;
import com.nam.service.UserService;
import com.nam.service.StudentService;
import com.nam.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify Spring Boot application context loads successfully
 * with H2 in-memory database configuration.
 * 
 * This test ensures:
 * - Spring context initializes without errors
 * - H2 database is properly configured
 * - Key beans (repositories, services) are available in the context
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:spring_context_testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
    "spring.jpa.show-sql=false"
})
@DisplayName("Spring Context Load Test with H2")
class SpringContextLoadTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private StudentRepository studentRepository;

    @Autowired(required = false)
    private RoleRepository roleRepository;

    @Autowired(required = false)
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired(required = false)
    private StudentPointRepository studentPointRepository;

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private StudentService studentService;

    @Autowired(required = false)
    private EmailService emailService;

    @Test
    @DisplayName("Should load Spring application context successfully")
    void contextLoads() {
        // Verify that the application context is not null
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("Should have UserRepository bean in context")
    void shouldHaveUserRepository() {
        assertThat(userRepository).isNotNull();
        assertThat(applicationContext.getBean(UserRepository.class)).isNotNull();
    }

    @Test
    @DisplayName("Should have StudentRepository bean in context")
    void shouldHaveStudentRepository() {
        assertThat(studentRepository).isNotNull();
        assertThat(applicationContext.getBean(StudentRepository.class)).isNotNull();
    }

    @Test
    @DisplayName("Should have RoleRepository bean in context")
    void shouldHaveRoleRepository() {
        assertThat(roleRepository).isNotNull();
        assertThat(applicationContext.getBean(RoleRepository.class)).isNotNull();
    }

    @Test
    @DisplayName("Should have RefreshTokenRepository bean in context")
    void shouldHaveRefreshTokenRepository() {
        assertThat(refreshTokenRepository).isNotNull();
        assertThat(applicationContext.getBean(RefreshTokenRepository.class)).isNotNull();
    }

    @Test
    @DisplayName("Should have StudentPointRepository bean in context")
    void shouldHaveStudentPointRepository() {
        assertThat(studentPointRepository).isNotNull();
        assertThat(applicationContext.getBean(StudentPointRepository.class)).isNotNull();
    }

    @Test
    @DisplayName("Should have UserService bean in context")
    void shouldHaveUserService() {
        assertThat(userService).isNotNull();
        assertThat(applicationContext.getBean(UserService.class)).isNotNull();
    }

    @Test
    @DisplayName("Should have StudentService bean in context")
    void shouldHaveStudentService() {
        assertThat(studentService).isNotNull();
        assertThat(applicationContext.getBean(StudentService.class)).isNotNull();
    }

    @Test
    @DisplayName("Should have EmailService bean in context")
    void shouldHaveEmailService() {
        assertThat(emailService).isNotNull();
        assertThat(applicationContext.getBean(EmailService.class)).isNotNull();
    }

    @Test
    @DisplayName("Should verify H2 database connection is established")
    void shouldVerifyH2DatabaseConnection() {
        // If repositories are available, it means H2 connection was established
        assertThat(userRepository).isNotNull();
        assertThat(studentRepository).isNotNull();
        
        // Verify that we can interact with the database (count should not throw exception)
        long userCount = userRepository.count();
        long studentCount = studentRepository.count();
        
        // Just verify the operation completes without exception
        assertThat(userCount).isGreaterThanOrEqualTo(0);
        assertThat(studentCount).isGreaterThanOrEqualTo(0);
    }
}
