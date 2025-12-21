package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.UserCreateVO;
import bg.tuvarna.devicebackend.models.dtos.UserUpdateVO;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.services.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTests {
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private DeviceService deviceService;
    @Autowired
    private UserService userService;

    @Test
    public void updateAdminUserPasswordShouldThrowException() {
        UserUpdateVO userUpdateVO = new UserUpdateVO(
                "gosho",
                "fake address",
                "0888888888",
                "gosho@email.com"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(
                User.builder()
                        .role(UserRole.ADMIN)
                        .build()
        ));

        CustomException ex = assertThrows(
                CustomException.class,
                () -> userService.updateUser(1L, userUpdateVO)
        );

        assertEquals("Admin password can't be changed", ex.getMessage());
    }

    @Test
    public void updateUserEmailWithAlreadyTakenShouldThrowException() {
        UserUpdateVO userUpdateVO = new UserUpdateVO(
                "gosho",
                "fake address",
                "0888888888",
                "gosho@email.com"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(
                User.builder()
                        .email("ujasno@email.com")
                        .build()
        ));

        when(userRepository.getByEmail("gosho@email.com")).thenReturn(
                User.builder()
                        .email("gosho@email.com")
                        .build()
        );

        CustomException ex = assertThrows(
                CustomException.class,
                () -> userService.updateUser(1L, userUpdateVO)
        );

        assertEquals("Email already taken", ex.getMessage());
    }

    @Test
    public void updateUserPhoneWithAlreadyTakenShouldThrowException() {
        UserUpdateVO userUpdateVO = new UserUpdateVO(
                "gosho",
                "fake address",
                "0888888888",
                "gosho@email.com"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(
                User.builder()
                        .phone("0777777777")
                        .build()
        ));

        when(userRepository.getByPhone("0888888888")).thenReturn(
                User.builder()
                        .phone("0888888888")
                        .build()
        );

        CustomException ex = assertThrows(
                CustomException.class,
                () -> userService.updateUser(1L, userUpdateVO)
        );

        assertEquals("Phone already taken", ex.getMessage());
    }
}