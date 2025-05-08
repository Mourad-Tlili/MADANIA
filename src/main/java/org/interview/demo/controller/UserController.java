package org.interview.demo.controller;

import org.interview.demo.model.User;
import org.interview.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // Helper method to check if a string contains only digits
    private boolean isStringNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) { // No @Valid annotation here
        String requestCin = (user != null && user.getCin() != null) ? user.getCin() : "null/not provided";
        logger.info("Received request to create user. Provided CIN for request: {}", requestCin);

        // --- Request Object Null Check ---
        if (user == null) {
            logger.warn("Validation failed: User object in request body is null.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User data cannot be null.");
        }

        // --- CIN Validation ---
        String cin = user.getCin();
        // 1. Not null or empty
        if (cin == null || cin.trim().isEmpty()) {
            logger.warn("Validation failed for createUser: CIN cannot be null or empty.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CIN cannot be null or empty.");
        }
        // 2. Length is 8
        if (cin.length() != 8) {
            logger.warn("Validation failed for createUser: CIN '{}' must be 8 characters long.", cin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CIN must be 8 characters long.");
        }
        // 3. Only numbers (using helper method)
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

        // --- Call Service ---
        try {
            User savedUser = userService.createUser(user);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) { // From service for duplicate CIN
            logger.warn("Conflict while creating user with CIN '{}': {}", cin, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (jakarta.validation.ConstraintViolationException cve) {
            logger.warn("Constraint violation during persistence for CIN '{}'. Violations: {}", requestCin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( "Data validation failed during save.");
        } catch (Exception e) { // Catch other unexpected exceptions
            logger.error("Unexpected error creating user with CIN '{}': {}", requestCin, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/cin/{cin}")
    public ResponseEntity<?> getUserByCin(@PathVariable String cin) {
        logger.info("Received request to get user by CIN: {}", cin);

        if (cin == null || cin.trim().isEmpty() || cin.length() != 8 || !isStringNumeric(cin)) {
            logger.warn("Validation failed for getUserByCin: Invalid CIN format in URL path variable '{}'.", cin);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid CIN format in URL.");
        }

        try {
            User user = userService.getUserByCin(cin);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warn("User not found with CIN '{}': {}", cin, e.getMessage());
            // Using the exception message directly might be okay if it's controlled (like from your service)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving user by CIN '{}': {}", cin, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving user by CIN.");
        }
    }
}
