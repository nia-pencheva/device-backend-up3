package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.models.entities.Renovation;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.DeviceRepository;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import bg.tuvarna.devicebackend.repositories.RenovationRepository;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@ActiveProfiles("test")
public class DeviceRepoTests {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PassportRepository passportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @BeforeEach
    void setUp() {
        Passport passport1 = Passport.builder()
                                    .name("FirstPassport")
                                    .model("FirstModel")
                                    .serialPrefix("First")
                                    .fromSerialNumber(1)
                                    .toSerialNumber(100)
                                    .warrantyMonths(36)
                                    .build();

        passport1 = passportRepository.save(passport1);

        Passport passport2 = Passport.builder()
                .name("SecondPassport")
                .model("AnotherModel")
                .serialPrefix("Second")
                .fromSerialNumber(200)
                .toSerialNumber(300)
                .warrantyMonths(36)
                .build();

        passport2 = passportRepository.save(passport2);

        User user1 = User.builder()
                            .fullName("Nia Pencheva")
                            .phone("0888888888")
                            .email("fake.email@gmail.com")
                            .role(UserRole.USER)
                            .build();

        user1 = userRepository.save(user1);

        User user2 = User.builder()
                .fullName("Petra Lewis")
                .phone("0888888887")
                .email("petralewis@gmail.com")
                .role(UserRole.USER)
                .build();

        user2 = userRepository.save(user2);

        Device device1 = new Device();
        device1.setSerialNumber("First1");
        device1.setPassport(passport1);
        device1.setUser(user1);
        device1.setPurchaseDate(LocalDate.now());
        device1.setWarrantyExpirationDate(LocalDate.now().plusMonths(12));

        deviceRepository.save(device1);

        Device device2 = new Device();
        device2.setSerialNumber("Second2");
        device2.setPassport(passport2);
        device2.setUser(user1);
        device2.setPurchaseDate(LocalDate.now());
        device2.setWarrantyExpirationDate(LocalDate.now().plusMonths(12));

        deviceRepository.save(device2);

        Device device3 = new Device();
        device3.setSerialNumber("First3");
        device3.setPassport(passport1);
        device3.setUser(user2);
        device3.setPurchaseDate(LocalDate.now());
        device3.setWarrantyExpirationDate(LocalDate.now().plusMonths(12));

        deviceRepository.save(device3);

        Renovation renovation = new Renovation();
        renovation.setDevice(device3);

        renovationRepository.save(renovation);
    }

    @AfterEach
    void tearDown() {
        renovationRepository.deleteAll();
        deviceRepository.deleteAll();
        passportRepository.deleteAll();
        userRepository.deleteAll();;
    }

    @Test
    void devicesFindAllByUserFullName() {
        Page<Device> devicePage = deviceRepository.findAll("Nia", PageRequest.of(0, 10));

        assertAll(
                () -> assertEquals(2, devicePage.getTotalElements()),
                () -> assertTrue(
                        devicePage.stream()
                                .allMatch(d -> "Nia Pencheva".equals(d.getUser().getFullName()))
                )
        );
    }

    @Test
    void devicesFindAllByPassportName() {
        Page<Device> devicePage = deviceRepository.findAll("FirstPassport", PageRequest.of(0, 10));

        assertAll(
                () -> assertEquals(2, devicePage.getTotalElements()),
                () -> assertTrue(
                        devicePage.stream()
                                .allMatch(d -> "FirstPassport".equals(d.getPassport().getName()))
                )
        );
    }
}
