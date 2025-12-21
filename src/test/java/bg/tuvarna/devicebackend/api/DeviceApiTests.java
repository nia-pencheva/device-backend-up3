package bg.tuvarna.devicebackend.api;

import bg.tuvarna.devicebackend.controllers.exceptions.ErrorResponse;
import bg.tuvarna.devicebackend.models.dtos.DeviceVO;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.DeviceRepository;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DeviceApiTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PassportRepository passportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Device device1;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        Passport passport1 = Passport.builder()
                .name("FirstPassport")
                .model("FirstModel")
                .serialPrefix("First")
                .fromSerialNumber(1)
                .toSerialNumber(100)
                .warrantyMonths(36)
                .build();

        passport1 = passportRepository.save(passport1);

        User user1 = User.builder()
                .fullName("Petra Lewis")
                .phone("0888888887")
                .email("petra@gmail.com")
                .role(UserRole.USER)
                .build();

        user1 = userRepository.save(user1);

        device1 = new Device();
        device1.setSerialNumber("First1");
        device1.setPassport(passport1);
        device1.setUser(user1);
        device1.setPurchaseDate(LocalDate.now());
        device1.setWarrantyExpirationDate(LocalDate.now().plusMonths(12));

        device1 = deviceRepository.save(device1);
    }

    @AfterEach
    void tearDown() {
        deviceRepository.deleteAll();
        userRepository.deleteAll();
        passportRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void findDeviceByIdAsAdmin_shouldReturnDevice() throws Exception {
        mvc.perform(get("/api/v1/devices/First1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("First1"));
    }

    @Test
    void deviceExists_shouldReturnDevice() throws Exception {
        mvc.perform(get("/api/v1/devices/exists/First1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("First1"));
    }

    @Test
    void deviceDoesNotExist_shouldThrowNotRegisteredException() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/devices/exists/Second1")).andReturn();

        assertEquals(400, result.getResponse().getStatus());

        ErrorResponse errorResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorResponse.class
        );

        Assertions.assertEquals("Device not registered", errorResponse.getError());
    }
}
