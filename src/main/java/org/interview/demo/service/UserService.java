package org.interview.demo.service;

import org.interview.demo.model.User;
import java.time.LocalDate;
import java.util.List; // For future getAllUsers
import java.util.Optional; // For future getUserById

/**
 * Service interface for User operations.
 */
public interface UserService {

    /**
     * Creates a new user.
     * @param user The user object to create.
     * @return The saved user.
     * @throws IllegalArgumentException if a user with the same CIN already exists.
     */
    User createUser(User user);

    /**
     * Retrieves a user by their CIN and CIN release date.
     * @param cin The user's CIN.
     * @param cinReleaseDate The release date of the CIN.
     * @return The found user.
     * @throws jakarta.persistence.EntityNotFoundException if no user is found.
     */
    User getUserByCinAndCinReleaseDate(String cin, LocalDate cinReleaseDate);

    // --- Placeholder for other typical CRUD operations ---
    /**
     * Retrieves all users.
     * @return A list of all users.
     */
    // List<User> getAllUsers();

    /**
     * Retrieves a user by their ID.
     * @param id The ID of the user.
     * @return An Optional containing the user if found.
     */
    // Optional<User> getUserById(Long id);

    /**
     * Updates an existing user.
     * @param id The ID of the user to update.
     * @param userDetails The user object with updated details.
     * @return The updated user.
     * @throws jakarta.persistence.EntityNotFoundException if the user to update is not found.
     */
    // User updateUser(Long id, User userDetails);

    /**
     * Deletes a user by their ID.
     * @param id The ID of the user to delete.
     * @throws jakarta.persistence.EntityNotFoundException if the user to delete is not found.
     */
    // void deleteUser(Long id);
}
