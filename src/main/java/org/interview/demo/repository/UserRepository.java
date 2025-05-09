package org.interview.demo.repository;

import org.interview.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository interface for User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their CIN.
     * Used for uniqueness checks.
     * @param cin The CIN to search for.
     * @return An Optional containing the User if found, or empty otherwise.
     */
    Optional<User> findByCin(String cin);

    /**
     * Finds a user by their CIN and CIN release date.
     * @param cin The CIN to search for.
     * @param cinReleaseDate The release date of the CIN.
     * @return An Optional containing the User if found, or empty otherwise.
     */
    Optional<User> findByCinAndCinReleaseDate(String cin, LocalDate cinReleaseDate);
}
