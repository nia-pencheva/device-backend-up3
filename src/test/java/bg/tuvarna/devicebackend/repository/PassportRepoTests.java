package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@ActiveProfiles("test")
public class PassportRepoTests {
    @Autowired
    private PassportRepository passportRepository;

    @BeforeEach
    void setUp() {
        Passport passport1 = Passport.builder()
                                .name("FirstPassport")
                                .model("FirstModel")
                                .serialPrefix("Test")
                                .fromSerialNumber(1)
                                .toSerialNumber(100)
                                .warrantyMonths(36)
                                .build();

        passport1 = passportRepository.save(passport1);

        Passport passport2 = Passport.builder()
                .name("SecondPassport")
                .model("SecondModel")
                .serialPrefix("Test")
                .fromSerialNumber(101)
                .toSerialNumber(200)
                .warrantyMonths(36)
                .build();

        passport2 = passportRepository.save(passport2);

        Passport passport3 = Passport.builder()
                .name("ThirdPassport")
                .model("ThridModel")
                .serialPrefix("Another")
                .fromSerialNumber(50)
                .toSerialNumber(150)
                .warrantyMonths(36)
                .build();

        passport3 = passportRepository.save(passport3);
    }

    @AfterEach
    void tearDown() {
        passportRepository.deleteAll();
    }

    @Test
    void passportsFindAllByFromSerialNumberBetween() {
        List<Passport> passports = passportRepository.findByFromSerialNumberBetween("Test", 20, 170);

        assertAll(
                () -> assertEquals(2, passports.size()),
                () -> assertTrue(
                        passports.stream()
                                .allMatch(p -> "Test".equals(p.getSerialPrefix()))
                ),
                () -> assertTrue(
                        passports.stream()
                                .allMatch(p -> (p.getFromSerialNumber() >= 20 && p.getFromSerialNumber() <= 170) || (p.getToSerialNumber() >= 20 && p.getToSerialNumber() <= 170))
                )
        );
    }

    @Test
    void passportsFindAllBySerial() {
        List<Passport> passports = passportRepository.findByFromSerial("Test123");

        assertAll(
                () -> assertEquals(2, passports.size()),
                () -> assertTrue(
                        passports.stream()
                                .allMatch(p -> "Test".equals(p.getSerialPrefix()))
                )
        );
    }
}
