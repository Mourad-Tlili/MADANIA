package org.interview.demo.service;

import org.interview.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(User user);

    // Method to get user by CIN
    User getUserByCin(String cin); // Throws exception if not found
}
