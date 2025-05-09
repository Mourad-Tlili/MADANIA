package org.interview.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.interview.demo.model.User;
import org.interview.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User userRequestDto;
    private User userResponseDto;
    private LocalDate releaseDate;
    private String releaseDateString;

    @BeforeEach
    void setUp() {
        releaseDate = LocalDate.of(2022, 5, 10);
        releaseDateString = releaseDate.format(DateTimeFormatter.ISO_DATE);

        userRequestDto = new User(null, "Test User", "12345678", releaseDate, true);
        userResponseDto = new User(1L, "Test User", "12345678", releaseDate, true);
    }

    @Test
    void whenCreateUser_withValidInput_shouldReturnCreatedUser() throws Exception {
        given(userService.createUser(any(User.class))).willReturn(userResponseDto);

        ResultActions response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)));

        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.name", is(userRequestDto.getName())))
                .andExpect(jsonPath("$.cin", is(userRequestDto.getCin())))
                .andExpect(jsonPath("$.cinReleaseDate", is(releaseDateString)))
                .andExpect(jsonPath("$.marriedStatus", is(userRequestDto.isMarriedStatus())));
    }
}
