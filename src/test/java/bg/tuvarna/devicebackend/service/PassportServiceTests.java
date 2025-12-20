package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.PassportUpdateVO;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import bg.tuvarna.devicebackend.services.PassportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class PassportServiceTests {
    @MockBean
    private PassportRepository passportRepository;

    @Autowired
    private PassportService passportService;

    @Test
    public void findPassportBySerialIdThrowsPassportNotFoundException() {
        when(passportRepository.findByFromSerial("1234")).thenReturn(new ArrayList<Passport>());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> passportService.findPassportBySerialId("1234")
        );

        assertEquals("Passport not found for serial number: 1234", ex.getMessage());
    }

    @Test
    public void updateThrowsPassportNotFound() {
        when(passportRepository.findById(1234L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> passportService.update(1234L, null)
        );

        assertEquals("Passport not found", ex.getMessage());
    }

    @Test
    public void updateThrowsSerialNumberAlreadyExistsException() {
        PassportUpdateVO passportUpdateVO = new PassportUpdateVO(
                "testPassport",
                "test",
                "differentPrefix",
                36,
                1,
                100
        );

        Long id = 1234L;

        when(passportRepository.findById(1234L)).thenReturn(Optional.of(new Passport(
                1234L,
                "testPassport",
                "test",
                "initialPrefix",
                36,
                1,
                100
        )));

        List<Passport> passportsWithNewSerialNumber = new ArrayList<Passport>();
        passportsWithNewSerialNumber.add(
            new Passport(
                    1235L,
                    "testPassport",
                    "test",
                    "initialPrefix",
                    36,
                    1,
                    100
            )
        );

        when(passportRepository.findByFromSerialNumberBetween("differentPrefix", 1, 100)).thenReturn(passportsWithNewSerialNumber);

        CustomException ex = assertThrows(
                CustomException.class,
                () -> passportService.update(id, passportUpdateVO)
        );

        assertEquals("Serial number already exists", ex.getMessage());
    }
}
