package com.boozebuddies.service.implementation;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.entity.User;
import com.boozebuddies.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final List<User> users = new ArrayList<>();
    private long nextUserId = 1;

    /**
     * Registers a new user in the system.
     */
    @Override
    public User register(User user) {
        if (user == null || user.getEmail() == null || user.getPasswordHash() == null) {
            throw new IllegalArgumentException("User email and password are required");
        }
        user.setId(nextUserId++);
        user.setAgeVerified(verifyAge(user));
        users.add(user);
        System.out.println("[USER REGISTER] User ID " + user.getId() + " registered with email " + user.getEmail());
        return user;
    }

    /**
     * Registers a new user from registration request DTO.
     */
    @Override
    public User registerUser(RegisterUserRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        
        User user = User.builder()
                .id(nextUserId++)
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(request.getPassword()) // In real app, hash this password!
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .build();
        
        user.setAgeVerified(verifyAge(user));
        users.add(user);
        
        System.out.println("[USER REGISTER] User ID " + user.getId() + " registered with email " + user.getEmail());
        return user;
    }

    /**
     * Authenticates a user with email and password.
     */
    @Override
    public User login(String email, String password) {
        Optional<User> userOpt = users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getPasswordHash().equals(password))
                .findFirst();
        return userOpt.orElse(null);
    }

    /**
     * Verifies that a user is of legal drinking age (21+ in the US).
     */
    @Override
    public boolean verifyAge(User user) {
        if (user == null || user.getDateOfBirth() == null) return false;
        int age = Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();
        return age >= 21;
    }

    /**
     * Retrieves a user by their unique ID.
     */
    @Override
    public Optional<User> getUserById(Long userId) {
        return users.stream().filter(u -> u.getId().equals(userId)).findFirst();
    }

    /**
     * Retrieves all users in the system.
     */
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Updates a user's information.
     */
    @Override
    public User updateUser(Long userId, User updatedUser) {
        Optional<User> userOpt = getUserById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
            if (updatedUser.getPasswordHash() != null) user.setPasswordHash(updatedUser.getPasswordHash());
            if (updatedUser.getDateOfBirth() != null) {
                user.setDateOfBirth(updatedUser.getDateOfBirth());
                user.setAgeVerified(verifyAge(user));
            }
            return user;
        }
        return null;
    }

    /**
     * Deletes a user from the system.
     */
    @Override
    public void deleteUser(Long userId) {
        users.removeIf(u -> u.getId().equals(userId));
        System.out.println("[USER DELETE] User ID " + userId + " deleted");
    }
}