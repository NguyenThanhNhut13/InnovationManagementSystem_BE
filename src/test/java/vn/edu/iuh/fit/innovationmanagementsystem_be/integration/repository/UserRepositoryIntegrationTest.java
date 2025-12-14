package vn.edu.iuh.fit.innovationmanagementsystem_be.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department testDepartment;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        testDepartment = new Department();
        testDepartment.setDepartmentName("Test Department");
        testDepartment.setDepartmentCode("TEST-DEPT-" + System.currentTimeMillis());
        testDepartment.setIsActive(true);
        testDepartment = departmentRepository.save(testDepartment);
    }

    private User createTestUser(String personnelId, String email) {
        User user = new User();
        user.setPersonnelId(personnelId);
        user.setFullName("Test User " + personnelId);
        user.setEmail(email);
        user.setPassword("hashedPassword123");
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setDepartment(testDepartment);
        return user;
    }

    // ==================== Save Tests ====================

    @Test
    @DisplayName("1. Save User - Should persist user to PostgreSQL")
    void testSaveUser_Success() {
        User user = createTestUser("TEST001", "test001@test.com");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("TEST001", savedUser.getPersonnelId());
        assertEquals("test001@test.com", savedUser.getEmail());
    }

    // ==================== Find Tests ====================

    @Test
    @DisplayName("2. Find by Email - Should find existing user")
    void testFindByEmail_Success() {
        User user = createTestUser("TEST002", "test002@test.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test002@test.com");

        assertTrue(found.isPresent());
        assertEquals("TEST002", found.get().getPersonnelId());
    }

    @Test
    @DisplayName("3. Find by Email - Should return empty for non-existent user")
    void testFindByEmail_NotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@test.com");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("4. Find by PersonnelId - Should find existing user")
    void testFindByPersonnelId_Success() {
        User user = createTestUser("TEST003", "test003@test.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByPersonnelId("TEST003");

        assertTrue(found.isPresent());
        assertEquals("test003@test.com", found.get().getEmail());
    }

    // ==================== Exists Tests ====================

    @Test
    @DisplayName("5. Exists by PersonnelId - Should return true for existing user")
    void testExistsByPersonnelId_True() {
        User user = createTestUser("TEST004", "test004@test.com");
        userRepository.save(user);

        boolean exists = userRepository.existsByPersonnelId("TEST004");

        assertTrue(exists);
    }

    @Test
    @DisplayName("6. Exists by PersonnelId - Should return false for non-existent user")
    void testExistsByPersonnelId_False() {
        boolean exists = userRepository.existsByPersonnelId("NONEXISTENT");

        assertFalse(exists);
    }

    @Test
    @DisplayName("7. Exists by Email - Should return true for existing user")
    void testExistsByEmail_True() {
        User user = createTestUser("TEST005", "test005@test.com");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test005@test.com");

        assertTrue(exists);
    }

    // ==================== Search Tests ====================

    @Test
    @DisplayName("8. Search by FullName - Should find matching users")
    void testSearchUsersByFullNameOrPersonnelId_ByName() {
        User user1 = createTestUser("TEST006", "test006@test.com");
        user1.setFullName("Nguyen Van A");
        userRepository.save(user1);

        User user2 = createTestUser("TEST007", "test007@test.com");
        user2.setFullName("Tran Van B");
        userRepository.save(user2);

        Page<User> results = userRepository.searchUsersByFullNameOrPersonnelId(
                "Nguyen", PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("Nguyen Van A", results.getContent().get(0).getFullName());
    }

    @Test
    @DisplayName("9. Search by PersonnelId - Should find matching users")
    void testSearchUsersByFullNameOrPersonnelId_ByPersonnelId() {
        User user = createTestUser("GV2024001", "gv001@test.com");
        user.setFullName("Test Teacher");
        userRepository.save(user);

        Page<User> results = userRepository.searchUsersByFullNameOrPersonnelId(
                "GV2024", PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("GV2024001", results.getContent().get(0).getPersonnelId());
    }

    // ==================== Department Tests ====================

    @Test
    @DisplayName("10. Find by DepartmentId - Should find users in department")
    void testFindByDepartmentId_Success() {
        User user1 = createTestUser("TEST008", "test008@test.com");
        User user2 = createTestUser("TEST009", "test009@test.com");
        userRepository.save(user1);
        userRepository.save(user2);

        var users = userRepository.findByDepartmentId(testDepartment.getId());

        assertEquals(2, users.size());
    }
}
