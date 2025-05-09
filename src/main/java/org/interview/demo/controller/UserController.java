package org.interview.demo.controller;

import org.interview.demo.model.User;
import org.interview.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException; // Keep for service layer exception

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap; // For structured error response
import java.util.Map;     // For structured error response
import java.util.stream.Collectors; // For structured error response

/**
 * REST Controller for managing users.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Helper method to check if a string contains only digits.
     * @param str The string to check.
     * @return true if the string contains only digits, false otherwise.
     */
    private boolean isStringNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false; // Handled by earlier checks, but good for standalone use
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new user.
     * @param user The user data from the request body.
     * @return ResponseEntity with the created user and HTTP status 201, or an error response.
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        String requestCin = (user != null && user.getCin() != null) ? user.getCin() : "null/not provided";
        logger.info("Received request to create user. Provided CIN for request: {}", requestCin);

        // --- Request Object Null Check ---
        if (user == null) {
            logger.warn("Validation failed for createUser: User object in request body is null.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User data cannot be null.");
        }

        // --- CIN Validation ---
        String cin = user.getCin();
        if (cin == null || cin.trim().isEmpty()) {
            logger.warn("Validation failed for createUser: CIN cannot be null or empty.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CIN cannot be null or empty.");
        }
        if (cin.length() != 8) {
            logger.warn("Validation failed for createUser: CIN '{}' must be 8 characters long.", cin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CIN must be 8 characters long.");
        }
        if (!isStringNumeric(cin)) {
            logger.warn("Validation failed for createUser: CIN '{}' must contain only numbers.", cin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CIN must contain only numbers.");
        }

        // --- Name Validation ---
        String name = user.getName();
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Validation failed for createUser with CIN '{}': Name cannot be null or empty.", cin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name cannot be null or empty.");
        }

        // --- CinReleaseDate Validation ---
        if (user.getCinReleaseDate() == null) {
            logger.warn("Validation failed for createUser with CIN '{}': CIN Release Date cannot be null.", cin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CIN Release Date cannot be null.");
        }
        // --- Date validations ---:
         if (user.getCinReleaseDate().isAfter(LocalDate.now())) {
             logger.warn("Validation failed for createUser with CIN '{}': CIN Release Date cannot be in the future.", cin);
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CIN Release Date cannot be in the future.");
         }

        try {
            User savedUser = userService.createUser(user);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) { // From service for duplicate CIN
            logger.warn("Conflict while creating user with CIN '{}': {}", cin, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (jakarta.validation.ConstraintViolationException cve) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Data validation failed during save."));
        } catch (Exception e) { // Catch other unexpected exceptions
            logger.error("Unexpected error creating user with CIN '{}': {}", requestCin, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Retrieves a user by their CIN and CIN release date.
     * @param cin The user's CIN (path variable).
     * @param releaseDate The release date of the CIN (request parameter, format YYYY-MM-DD).
     * @return ResponseEntity with the found user and HTTP status 200, or an error response.
     */
    @GetMapping("/cin/{cin}") // Example: /api/v1/users/cin/12345678?releaseDate=2023-01-15
    public ResponseEntity<?> getUserByCinAndReleaseDate(
            @PathVariable String cin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate) {

        logger.info("Received request to get user by CIN: {} and ReleaseDate: {}", cin, releaseDate);

        // --- CIN PathVariable Validation ---
        if (cin == null || cin.trim().isEmpty() || cin.length() != 8 || !isStringNumeric(cin)) {
            logger.warn("Validation failed for getUserByCinAndReleaseDate: Invalid CIN format in URL path variable '{}'.", cin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid CIN format in URL.");
        }
        // --- ReleaseDate RequestParam Validation ---
        if (releaseDate == null) {
            logger.warn("Validation failed for getUserByCinAndReleaseDate with CIN '{}': Release Date parameter cannot be null.", cin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Release Date parameter ('releaseDate') cannot be null.");
        }

        try {
            User user = userService.getUserByCinAndCinReleaseDate(cin, releaseDate);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warn("User not found with CIN '{}' and ReleaseDate '{}': {}", cin, releaseDate, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving user by CIN '{}' and ReleaseDate '{}': {}", cin, releaseDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving user.");
        }
    }
}
