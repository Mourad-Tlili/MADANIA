package org.interview.demo.service;

import jakarta.persistence.EntityNotFoundException;
import org.interview.demo.model.User;
import org.interview.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
// import java.util.List; // For future getAllUsers
// import java.util.Optional; // For future getUserById

/**
 * Implementation of the UserService interface.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        logger.info("Attempting to create user. CIN: {}, ReleaseDate: {}", user.getCin(), user.getCinReleaseDate());

        // Check if a user with the same CIN string already exists (as per current DB unique constraint on CIN)
        if (userRepository.findByCin(user.getCin()).isPresent()) {
            String errorMessage = "User with CIN " + user.getCin() + " already exists.";
            logger.warn("User creation failed: {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // You might also want a business rule to check if the exact combination of
        // CIN and CinReleaseDate already exists, if that's a unique business key.
        // Example:
        // if (userRepository.findByCinAndCinReleaseDate(user.getCin(), user.getCinReleaseDate()).isPresent()) {
        //     String errorMessage = "User with CIN " + user.getCin() + " and ReleaseDate " + user.getCinReleaseDate() + " already exists.";
        //     logger.warn("User creation failed: {}", errorMessage);
        //     throw new IllegalArgumentException(errorMessage);
        // }

        logger.debug("Saving new user with CIN: {}", user.getCin());
        User savedUser = userRepository.save(user);
        logger.info("Successfully created user with ID {} and CIN {}", savedUser.getId(), savedUser.getCin());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByCinAndCinReleaseDate(String cin, LocalDate cinReleaseDate) {
        logger.debug("Attempting to find user by CIN: {} and CinReleaseDate: {}", cin, cinReleaseDate);
        return userRepository.findByCinAndCinReleaseDate(cin, cinReleaseDate)
                .orElseThrow(() -> {
                    String errorMessage = "User not found with CIN: " + cin + " and Release Date: " + cinReleaseDate;
                    logger.warn(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });
    }

    // --- Implementations for other UserService methods would go here ---
    // public List<User> getAllUsers() { ... }
    // public Optional<User> getUserById(Long id) { ... }
    // public User updateUser(Long id, User userDetails) { ... }
    // public void deleteUser(Long id) { ... }
}
