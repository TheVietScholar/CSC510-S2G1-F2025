package com.boozebuddies.service.implementation;

import com.boozebuddies.model.User;
import com.boozebuddies.model.Product;
import com.boozebuddies.service.ValidationService;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ValidationServiceImpl implements ValidationService {

    // Simple regex for email validation
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Password must be at least 8 characters, contain letters and numbers
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    /**
     * Validates a user's email format.
     */
    @Override
    public boolean validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates a user's password strength.
     */
    @Override
    public boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validates that a user is of legal drinking age (21+).
     */
    @Override
    public boolean validateAge(User user) {
        if (user == null || user.getDateOfBirth() == null) {
            return false;
        }
        int legalAge = 21;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.Period age = java.time.Period.between(user.getDateOfBirth(), today);
        return age.getYears() >= legalAge;
    }

    /**
     * Validates that a product has valid data.
     */
    @Override
    public boolean validateProduct(Product product) {
        if (product == null) {
            return false;
        }
        if (product.getName() == null || product.getName().isEmpty()) {
            return false;
        }
        if (product.getPrice() == null || product.getPrice() < 0) {
            return false;
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            return false;
        }
        if (product.getType() == null || product.getType().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Validates that a given quantity is positive and available in stock.
     */
    @Override
    public boolean validateProductQuantity(Product product, int quantity) {
        if (product == null) {
            return false;
        }
        if (quantity <= 0) {
            return false;
        }
        return product.getStockQuantity() >= quantity;
    }

    
}
