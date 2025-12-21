package bg.tuvarna.devicebackend.api;

import bg.tuvarna.devicebackend.controllers.exceptions.ErrorResponse;
import bg.tuvarna.devicebackend.models.dtos.PassportVO;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PassportApiTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PassportRepository passportRepository;

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
    @WithMockUser(authorities = "ADMIN")
    void createPassportAsAdmin_shouldSucceedAndReturnNewPassport() throws Exception {
        MvcResult result = mvc.perform(
                post("/api/v1/passports")
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
        ).andExpect(status().isCreated()).andReturn();

        PassportVO passportVO = mapper.readValue(
                result.getResponse().getContentAsString(),
                PassportVO.class
        );

        assertEquals("New Passport", passportVO.name());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createAlreadyExistingPassport_shouldThrowSerialNumberExistsException() throws Exception {
        MvcResult result = mvc.perform(
                post("/api/v1/passports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "name": "New Passport",
                                        "model": "New Passport",
                                        "serialPrefix": "First",
                                        "warrantyMonths": 36,
                                        "fromSerialNumber": 30,
                                        "toSerialNumber": 150
                                    }
                                """)
        ).andReturn();

       assertEquals(400, result.getResponse().getStatus());

        ErrorResponse errorResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorResponse.class
        );

        assertEquals("Serial number already exists", errorResponse.getError());
    }
}
