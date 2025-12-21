package bg.tuvarna.devicebackend.integrational;

import bg.tuvarna.devicebackend.controllers.exceptions.ErrorResponse;
import bg.tuvarna.devicebackend.models.dtos.AuthResponseDTO;
import bg.tuvarna.devicebackend.models.dtos.PassportVO;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.print.attribute.standard.Media;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PassportCRUDTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PassportRepository passportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String token;
    private static Long newPassportId;

    @BeforeAll
    void setupDatabase() {
        passportRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(
                User.builder()
                        .fullName("Admin")
                        .email("admin")
                        .phone("123456789")
                        .password(passwordEncoder.encode("admin"))
                        .role(UserRole.ADMIN)
                        .build()
        );

        Passport passport1 = Passport.builder()
                .name("FirstPassport")
                .model("FirstModel")
                .serialPrefix("First")
                .fromSerialNumber(1)
                .toSerialNumber(100)
                .warrantyMonths(36)
                .build();

        passportRepository.save(passport1);
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Order(1)
    void adminLogin_shouldSucceed() throws Exception {
        MvcResult login = mvc
                .perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "admin"
                                        }
                                        """)
                ).andReturn();

        assertEquals(200, login.getResponse().getStatus());

        AuthResponseDTO authResponseDTO = mapper.readValue(
                login.getResponse().getContentAsString(),
                AuthResponseDTO.class
        );

        token = authResponseDTO.getToken();
    }

    @Test
    @Order(2)
    void createExistingPassport_shouldThrowSerialNumberExistsException() throws Exception {
        List<Passport> passports = passportRepository.findAll();
        mvc.perform(
            post("/api/v1/passports")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": "New Passport",
                                "model": "New Passport",
                                "serialPrefix": "First",
                                "warrantyMonths": 36,
                                "fromSerialNumber": 1,
                                "toSerialNumber": 100
                            }
                            """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Serial number already exists"));
    }

    @Test
    @Order(3)
    void createNewPassport_shouldReturnNewPassport() throws Exception {
        MvcResult result = mvc.perform(
                post("/api/v1/passports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "name": "New Passport",
                                        "model": "New Passport",
                                        "serialPrefix": "New",
                                        "warrantyMonths": 36,
                                        "fromSerialNumber": 1,
                                        "toSerialNumber": 100
                                    }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("New Passport"))
                .andReturn();

        PassportVO passportVO = mapper.readValue(
                result.getResponse().getContentAsString(),
                PassportVO.class
        );

        newPassportId = passportVO.id();
    }

    @Test
    @Order(4)
    void updateNewlyCreatedPassportName_shouldReturnUpdatedPassport() throws Exception {
        mvc.perform(
                put("/api/v1/passports/{id}", newPassportId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Edited Passport",
                                    "model": "New Passport",
                                    "serialPrefix": "New",
                                    "warrantyMonths": 36,
                                    "fromSerialNumber": 1,
                                    "toSerialNumber": 100
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Edited Passport"));
    }

    @Test
    @Order(5)
    void updateNonExistentPassportName_shouldThrowNotFoundException() throws Exception {
        mvc.perform(
                        put("/api/v1/passports/{id}", 1234L)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "name": "New Passport",
                                        "model": "New Passport",
                                        "serialPrefix": "First",
                                        "warrantyMonths": 36,
                                        "fromSerialNumber": 1,
                                        "toSerialNumber": 100
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Passport not found"));
    }

    @Test
    @Order(6)
    void deletePassport_shouldSucceed() throws Exception {
        mvc.perform(
            delete("/api/v1/passports/{id}", newPassportId)
                    .header("Authorization", "Bearer " + token)
        ).andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void getDeletedPassportBySerialId_shouldThrowNotFoundException() throws Exception {
        mvc.perform(
                get("/api/v1/passports/getBySerialId/New1")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Passport not found for serial number: New1"));
    }

    @Test
    @Order(8)
    void registerUser_shouldSucceed() throws Exception {
        mvc.perform(
                post("/api/v1/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "User",
                                  "email": "user@email.com",
                                  "phone": "0888888888",
                                  "address": "address",
                                  "password": "Up3pass!"
                                }
                                """)
        ).andExpect(status().isOk());
    }

    @Test
    @Order(9)
    void loginUser_shouldSucceed() throws Exception {
        MvcResult login = mvc
                .perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "user@email.com",
                                          "password": "Up3pass!"
                                        }
                                        """)
                ).andReturn();

        assertEquals(200, login.getResponse().getStatus());

        AuthResponseDTO authResponseDTO = mapper.readValue(
                login.getResponse().getContentAsString(),
                AuthResponseDTO.class
        );

        token = authResponseDTO.getToken();
    }

    @Test
    @Order(10)
    void getPassportBySerialIdAsUser_shouldSucceed() throws Exception {
        mvc.perform(
                get("/api/v1/passports/getBySerialId/First1")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("FirstPassport"));
    }

    @Test
    @Order(11)
    void createPassportAsUser_shouldThrowForbiddenException() throws Exception {
        mvc.perform(
                post("/api/v1/passports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "name": "New Passport",
                                        "model": "New Passport",
                                        "serialPrefix": "New",
                                        "warrantyMonths": 36,
                                        "fromSerialNumber": 1,
                                        "toSerialNumber": 100
                                    }
                                """)
        ).andExpect(status().isForbidden());
    }
}
