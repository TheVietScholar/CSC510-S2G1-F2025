package com.boozebuddies.service;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Registers a new user in the system.
     *
     * @param user The user to register.
     * @return The registered user with generated ID.
     */
    User register(User user);

    /**
     * Registers a new user from registration request DTO.
     *
     * @param request The registration request containing user details.
     * @return The registered user with generated ID.
     */
    User registerUser(RegisterUserRequest request);

    /**
     * Authenticates a user with email and password.
     *
     * @param email The user's email.
     * @param password The user's password (hashed or plaintext depending on implementation).
     * @return The authenticated user if credentials are correct, otherwise null.
     */
    User login(String email, String password);

    /**
     * Verifies that a user is of legal drinking age (e.g., 21+ in the US).
     *
     * @param user The user to verify.
     * @return True if the user is of legal age, false otherwise.
     */
    boolean verifyAge(User user);

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the user if found, otherwise empty.
     */
    Optional<User> getUserById(Long userId);

    /**
     * Retrieves all users in the system.
     *
     * @return A list of all users.
     */
    List<User> getAllUsers();

    /**
     * Updates a user's information.
     *
     * @param userId The ID of the user to update.
     * @param user The user object containing updated information.
     * @return The updated user.
     */
    User updateUser(Long userId, User user);

    /**
     * Deletes a user from the system.
     *
     * @param userId The ID of the user to delete.
     */
    void deleteUser(Long userId);
}