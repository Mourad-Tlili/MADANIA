package org.interview.demo.service;

import jakarta.persistence.EntityNotFoundException;
import org.interview.demo.model.User;
import org.interview.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(User user) {

        // Check if CIN already exists
        if (userRepository.findByCin(user.getCin()).isPresent()) {
            logger.warn("User creation failed: CIN {} already exists.", user.getCin());
            throw new IllegalArgumentException("User with CIN " + user.getCin() + " already exists.");
        }
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByCin(String cin) {
        logger.debug("Attempting to find user by CIN: {}", cin);
        User user = userRepository.findByCin(cin)
                .orElseThrow(() -> {
                    logger.warn("User not found with CIN: {}", cin);
                    return new EntityNotFoundException("User not found with CIN: " + cin);
                });
        logger.debug("User found by CIN {}: {}", cin, user);
        return user;
    }

}
