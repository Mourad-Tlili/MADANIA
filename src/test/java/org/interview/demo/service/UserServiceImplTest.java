package org.interview.demo.service;

import org.interview.demo.model.User;
import org.interview.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*; // Import standard Mockito methods

/**
 * Unit tests for {@link UserServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock // Creates a mock instance of UserRepository
    private UserRepository userRepository;

    @InjectMocks // Creates UserServiceImpl and injects mocks (userRepository)
    // Assumes UserServiceImpl uses constructor injection
    private UserServiceImpl userService;

    private User userToCreate;
    private User existingUser;
    private LocalDate commonReleaseDate;

    @BeforeEach
    void setUp() {
        commonReleaseDate = LocalDate.of(2022, 1, 15);
        // User to be used in creation tests
        userToCreate = new User(null, "New User", "CINNEW01", commonReleaseDate, true);
        // An existing user for other tests
        existingUser = new User(1L, "Existing User", "CINEXIST", commonReleaseDate, false);
    }

    // --- Tests for createUser ---

    @Test
    void createUser_whenCinIsNew_shouldSaveAndReturnUserWithId() {
        // Arrange
        // 1. When repository checks for existing CIN, find nothing
        when(userRepository.findByCin(userToCreate.getCin())).thenReturn(Optional.empty());

        // 2. When repository saves the user, simulate it gets an ID and returns it
        User userAfterSave = new User(1L,
                userToCreate.getName(),
                userToCreate.getCin(),
                userToCreate.getCinReleaseDate(),
                userToCreate.isMarriedStatus()); //
        when(userRepository.save(userToCreate)).thenReturn(userAfterSave);

        // Act
        User result = userService.createUser(userToCreate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L); // Verify ID is set
        assertThat(result.getName()).isEqualTo(userToCreate.getName());
        assertThat(result.getCin()).isEqualTo(userToCreate.getCin());
        assertThat(result.isMarriedStatus()).isEqualTo(userToCreate.isMarriedStatus());


        // Verify that the expected repository methods were called
        verify(userRepository).findByCin(userToCreate.getCin());
        verify(userRepository).save(userToCreate);
    }

    @Test
    void createUser_whenCinAlreadyExists_shouldThrowIllegalArgumentException() {
        // Arrange
        // When repository checks for existing CIN, it finds one
        when(userRepository.findByCin(userToCreate.getCin())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(userToCreate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with CIN " + userToCreate.getCin() + " already exists.");

        // Verify that save was never called because the CIN check should prevent it
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Tests for getUserByCinAndCinReleaseDate ---

    @Test
    void getUserByCinAndCinReleaseDate_whenUserExists_shouldReturnUser() {
        // Arrange
        // When repository searches by CIN and date, return the existingUser
        when(userRepository.findByCinAndCinReleaseDate(existingUser.getCin(), existingUser.getCinReleaseDate()))
                .thenReturn(Optional.of(existingUser));

        // Act
        User result = userService.getUserByCinAndCinReleaseDate(existingUser.getCin(), existingUser.getCinReleaseDate());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingUser.getId());
        assertThat(result.getName()).isEqualTo(existingUser.getName());
        // You can compare the whole object if equals/hashCode is well-defined based on ID
        // assertThat(result).isEqualTo(existingUser);
    }

    @Test
    void getUserByCinAndCinReleaseDate_whenUserDoesNotExist_shouldThrowEntityNotFoundException() {
        // Arrange
        String unknownCin = "UNKNOWN1";
        LocalDate unknownDate = LocalDate.of(2000, 1, 1);
        // When repository searches, find nothing
        when(userRepository.findByCinAndCinReleaseDate(unknownCin, unknownDate)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByCinAndCinReleaseDate(unknownCin, unknownDate))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with CIN: " + unknownCin + " and Release Date: " + unknownDate);
    }
}
