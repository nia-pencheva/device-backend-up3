package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.controllers.exceptions.ErrorCode;
import bg.tuvarna.devicebackend.models.dtos.DeviceCreateVO;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.repositories.DeviceRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.services.PassportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class DeviceServiceTests {
    @MockBean
    private PassportService passportService;

    @MockBean
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceService deviceService;

    @Test
    public void registerDeviceShouldThrowInvalidSerialNumberException() {
        when(passportService.findPassportBySerialId("1234")).thenThrow(new CustomException("Passport not found for serial number: 1234", ErrorCode.Failed));

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.registerDevice("1234", LocalDate.now(), new User())
        );

        assertEquals("Invalid serial number", ex.getMessage());
    }

    @Test
    public void registerNewDeviceThrowsUserNotFound() {
        DeviceCreateVO deviceCreateVO = new DeviceCreateVO(
             "1234",
             LocalDate.now()
        );

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.registerNewDevice(deviceCreateVO, null)
        );

        assertEquals("User not found", ex.getMessage());
    }
}
