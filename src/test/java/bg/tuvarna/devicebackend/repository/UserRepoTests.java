package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepoTests {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user1 = User.builder()
                .fullName("losho")
                .email("losho@abv.bg")
                .phone("0888123457")
                .role(UserRole.ADMIN)
                .build();

        userRepository.save(user1);

        User user2 = User.builder()
                .fullName("mosho")
                .email("mosho@abv.bg")
                .phone("0888123458")
                .role(UserRole.USER)
                .build();

        userRepository.save(user2);

        User user3 = User.builder()
                .fullName("gosho")
                .email("gosho@abv.bg")
                .phone("0888123456")
                .role(UserRole.USER)
                .build();

        userRepository.save(user3);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void getAllUsersWithoutAdmins() {
        Page<User> usersPage = userRepository.getAllUsers(PageRequest.of(0, 10));

        assertAll(
                () -> assertEquals(2, usersPage.getTotalElements()),
                () -> assertTrue(
                        usersPage.stream()
                                .allMatch(u -> UserRole.USER.equals(u.getRole()))
                )
        );
    }
}