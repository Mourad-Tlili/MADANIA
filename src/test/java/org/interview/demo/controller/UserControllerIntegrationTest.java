package org.interview.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.interview.demo.model.User;
import org.interview.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers // Enables Testcontainers JUnit 5 extension
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Loads full app context
@AutoConfigureMockMvc // Configures MockMvc for calling the application
public class UserControllerIntegrationTest {

    @Container // Marks this as a Testcontainer-managed container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("test_user_db")
            .withUsername("test_user")
            .withPassword("test_password");
    // .withReuse(true); // You can add this back later if startup time is an issue and it's stable

    @DynamicPropertySource
    static void configureTestDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // Fresh schema for test class
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.show-sql", () -> "true"); // Enable for test debugging if needed
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User userRequestPayload;
    private LocalDate commonReleaseDate;
    private String commonReleaseDateString;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        commonReleaseDate = LocalDate.of(2022, 5, 10);
        commonReleaseDateString = commonReleaseDate.format(DateTimeFormatter.ISO_DATE);
        // Use a VALID NUMERIC CIN
        userRequestPayload = new User(null, "Integration User", "12345678", commonReleaseDate, true);
    }

    @AfterEach // Clean up database after each test method for isolation
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void whenCreateUser_withValidInput_shouldSaveUserAndReturnCreated() throws Exception {
        // Act
        ResultActions response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestPayload)));

        // Assert
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(userRequestPayload.getName())))
                .andExpect(jsonPath("$.cin", is(userRequestPayload.getCin())))
                .andExpect(jsonPath("$.cinReleaseDate", is(commonReleaseDateString)))
                .andExpect(jsonPath("$.marriedStatus", is(userRequestPayload.isMarriedStatus()))); // Uses standard isMarried()

        // Verify data in database
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        User savedDbUser = users.get(0);
        assertThat(savedDbUser.getCin()).isEqualTo(userRequestPayload.getCin());
        assertThat(savedDbUser.getName()).isEqualTo(userRequestPayload.getName());
        assertThat(savedDbUser.getCinReleaseDate()).isEqualTo(userRequestPayload.getCinReleaseDate());
        assertThat(savedDbUser.isMarriedStatus()).isEqualTo(userRequestPayload.isMarriedStatus()); // Uses standard isMarried()
    }

    @Test
    void whenCreateUser_withInvalidCinLength_shouldReturnBadRequest() throws Exception {
        userRequestPayload.setCin("123"); // Invalid length

        ResultActions response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestPayload)));

        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CIN must be 8 characters long."));

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void whenCreateUser_withCinNotNumeric_shouldReturnBadRequest() throws Exception {
        userRequestPayload.setCin("ABCDEFGH"); // Not numeric

        ResultActions response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestPayload)));

        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CIN must contain only numbers."));

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void whenCreateUser_withNullName_shouldReturnBadRequest() throws Exception {
        userRequestPayload.setName(null);

        ResultActions response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestPayload)));

        response.andExpect(status().isBadRequest())
                .andExpect(content().string("Name cannot be null or empty."));
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void whenCreateUser_withNullCinReleaseDate_shouldReturnBadRequest() throws Exception {
        userRequestPayload.setCinReleaseDate(null);

        ResultActions response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestPayload)));

        response.andExpect(status().isBadRequest())
                .andExpect(content().string("CIN Release Date cannot be null."));
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void whenCreateUser_withDuplicateCin_shouldReturnConflict() throws Exception {
        // Arrange: Save a user first to create a duplicate CIN scenario
        // Use a different name/date to ensure it's a distinct record attempt if CIN wasn't unique
        User existingUser = new User(null, "Existing User", userRequestPayload.getCin(), LocalDate.now().minusYears(1), false);
        userRepository.save(existingUser); // User with CIN "12345678" now exists

        // Act: Try to create another user with the same CIN
        ResultActions response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestPayload))); // userRequestPayload also has CIN "12345678"

        // Assert
        response.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().string("User with CIN " + userRequestPayload.getCin() + " already exists."));

        assertThat(userRepository.count()).isEqualTo(1); // Only the first user should be in DB
    }


    @Test
    void whenGetUserByCinAndReleaseDate_userExists_shouldReturnUser() throws Exception {
        // Arrange: Save a user first
        User savedUser = userRepository.save(userRequestPayload); // Saves with CIN "12345678" and commonReleaseDate

        // Act
        ResultActions response = mockMvc.perform(get("/api/v1/users/cin/{cin}", savedUser.getCin())
                .param("releaseDate", savedUser.getCinReleaseDate().format(DateTimeFormatter.ISO_DATE))
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedUser.getId().intValue())))
                .andExpect(jsonPath("$.cin", is(savedUser.getCin())))
                .andExpect(jsonPath("$.name", is(savedUser.getName())))
                .andExpect(jsonPath("$.cinReleaseDate", is(commonReleaseDateString)))
                .andExpect(jsonPath("$.marriedStatus", is(savedUser.isMarriedStatus()))); // Uses standard isMarried()
    }

    @Test
    void whenGetUserByCinAndReleaseDate_userDoesNotExist_shouldReturnNotFound() throws Exception {
        String nonExistentCin = "00000000";
        LocalDate nonExistentDate = LocalDate.of(2000, 1, 1);

        ResultActions response = mockMvc.perform(get("/api/v1/users/cin/{cin}", nonExistentCin)
                .param("releaseDate", nonExistentDate.format(DateTimeFormatter.ISO_DATE))
                .accept(MediaType.APPLICATION_JSON));

        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with CIN: " + nonExistentCin + " and Release Date: " + nonExistentDate));
    }

    @Test
    void whenGetUserByCinAndReleaseDate_withInvalidCinFormatInPath_shouldReturnBadRequest() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/users/cin/{cin}", "INVALID") // Invalid CIN
                .param("releaseDate", commonReleaseDateString)
                .accept(MediaType.APPLICATION_JSON));

        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid CIN format in URL."));
    }

    @Test
    void whenGetUserByCinAndReleaseDate_withMissingReleaseDateParam_shouldReturnBadRequestFromController() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/users/cin/{cin}", "12345678")
                // releaseDate parameter is missing
                .accept(MediaType.APPLICATION_JSON));

        response.andDo(print())
                .andExpect(status().isBadRequest())
                // This message comes from your controller's manual null check for releaseDate
                .andExpect(content().string("Release Date parameter ('releaseDate') cannot be null."));
    }
}
