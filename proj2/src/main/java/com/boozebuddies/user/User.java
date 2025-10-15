//Needs to be changed to actual package
package com.boozebuddies.user;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

public class User {

	private static Map<String, User> userDatabase = new HashMap<>(); // simple in-memory storage

	private Long userId;
	private String email;
	private String passwordHash;
	private LocalDate dateOfBirth;
	private UserType userType;

	public User(Long userId, String email, String passwordHash, LocalDate dateOfBirth, UserType userType) {
		this.userId = userId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.dateOfBirth = dateOfBirth;
		this.userType = userType;
	}

	public User() {
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public boolean verifyAge() {
		if (dateOfBirth == null) {
			return false;
		}
		return Period.between(dateOfBirth, LocalDate.now()).getYears() >= 21;
	}

	public static boolean register(String email, String password, LocalDate dateOfBirth, UserType userType) {
		if (userDatabase.containsKey(email)) {
			System.out.println("Error: User already registered.");
			return false;
		}

		User newUser = new User(System.currentTimeMillis(), email, hashPassword(password), dateOfBirth, userType);

		if (!newUser.verifyAge()) {
			System.out.println("Error: Must be 21 or older to register.");
			return false;
		}

		userDatabase.put(email, newUser);
		System.out.println("Registration successful for " + email);
		return true;
	}

	public static boolean login(String email, String password) {
		User user = userDatabase.get(email);
		if (user == null) {
			System.out.println("Error: No user found with that email.");
			return false;
		}

		if (!hashPassword(password).equals(user.passwordHash)) {
			System.out.println(" Error: Incorrect password.");
			return false;
		}

		System.out.println(" Login successful! Welcome, " + email);
		return true;
	}

	// Simple password hashing (demo purposes)
	private static String hashPassword(String password) {
		return Integer.toHexString(password.hashCode());
	}

	@Override
	public String toString() {
		return "User{" + "userId=" + userId + ", email='" + email + '\'' + ", userType=" + userType + '}';
	}
}
