package com.boozebuddies.config;

import com.boozebuddies.entity.Certification;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.CertificationStatus;
import com.boozebuddies.model.Role;
import com.boozebuddies.repository.DriverRepository;
import com.boozebuddies.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

  @Autowired private UserRepository userRepository;

  @Autowired private DriverRepository driverRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    createAdminUser();
    createMerchantAdmin();
    createUser();
    createDriverUser();
  }

  private void createUser() {
    // Check if user already exists by email
    Optional<User> existingUser = userRepository.findByEmailIgnoreCase("user@boozebuddies.com");
    if (existingUser.isEmpty()) {
      User user =
          User.builder()
              .name("User")
              .email("user@boozebuddies.com")
              .passwordHash(passwordEncoder.encode("password"))
              .isActive(true)
              .isEmailVerified(true)
              .ageVerified(true)
              .build();

      // Add USER role
      user.addRole(Role.USER);

      userRepository.save(user);
      System.out.println("=== USER CREATED ===");
      System.out.println("Email: user@boozebuddies.com");
      System.out.println("Password: password");
      System.out.println("Role: USER");
      System.out.println("==========================");
    } else {
      System.out.println("User already exists");
    }
  }

  private void createAdminUser() {
    // Check if admin user already exists by email
    Optional<User> existingAdmin = userRepository.findByEmailIgnoreCase("admin@boozebuddies.com");
    if (existingAdmin.isEmpty()) {
      User admin =
          User.builder()
              .name("System Administrator")
              .email("admin@boozebuddies.com")
              .passwordHash(passwordEncoder.encode("password")) // This will be properly hashed!
              .isActive(true)
              .isEmailVerified(true)
              .ageVerified(true)
              .build();

      // Add ADMIN role
      admin.addRole(Role.ADMIN);

      userRepository.save(admin);
      System.out.println("=== ADMIN USER CREATED ===");
      System.out.println("Email: admin@boozebuddies.com");
      System.out.println("Password: password");
      System.out.println("Role: ADMIN");
      System.out.println("==========================");
    } else {
      System.out.println("Admin user already exists");
    }
  }

  private void createMerchantAdmin() {
    // Check if merchant admin already exists by email
    Optional<User> existingMerchantAdmin =
        userRepository.findByEmailIgnoreCase("merchant1@boozebuddies.com");
    if (existingMerchantAdmin.isEmpty()) {
      User merchantAdmin =
          User.builder()
              .name("Merchant Manager")
              .email("merchant1@boozebuddies.com")
              .passwordHash(passwordEncoder.encode("password")) // Same password for demo
              .isActive(true)
              .isEmailVerified(true)
              .ageVerified(true)
              .merchantId(1L) // Attach to merchant ID 1
              .build();

      // Add MERCHANT_ADMIN role
      merchantAdmin.addRole(Role.MERCHANT_ADMIN);

      userRepository.save(merchantAdmin);
      System.out.println("=== MERCHANT ADMIN USER CREATED ===");
      System.out.println("Email: merchant1@boozebuddies.com");
      System.out.println("Password: password");
      System.out.println("Role: MERCHANT_ADMIN");
      System.out.println("Merchant ID: 1");
      System.out.println("==========================");
    } else {
      System.out.println("Merchant admin user already exists");
    }
  }

  private void createDriverUser() {
    // Check if driver user already exists by email
    Optional<User> existingDriverUser =
        userRepository.findByEmailIgnoreCase("driver@boozebuddies.com");
    if (existingDriverUser.isEmpty()) {
      // Create the User first
      User driverUser =
          User.builder()
              .name("Demo Driver")
              .email("driver@boozebuddies.com")
              .passwordHash(passwordEncoder.encode("password"))
              .isActive(true)
              .isEmailVerified(true)
              .ageVerified(true)
              .latitude(35.7800) // Raleigh area coordinates
              .longitude(-78.6380)
              .build();

      // Add DRIVER role
      driverUser.addRole(Role.DRIVER);

      // Save the user first
      User savedUser = userRepository.save(driverUser);

      // Create the Driver entity linked to the user
      Certification certification =
          Certification.builder()
              .certificationNumber("CERT-DRV-001")
              .certificationType("Alcohol Delivery")
              .issueDate(LocalDate.now().minusMonths(6))
              .expiryDate(LocalDate.now().plusYears(1))
              .valid(true)
              .build();

      Driver driver =
          Driver.builder()
              .user(savedUser)
              .name("Demo Driver")
              .email("driver@boozebuddies.com")
              .phone("(555) 123-4567")
              .vehicleType("Car")
              .licensePlate("DRV-001")
              .isAvailable(true)
              .currentLatitude(35.7800)
              .currentLongitude(-78.6380)
              .rating(4.8)
              .totalDeliveries(0)
              .certificationStatus(CertificationStatus.APPROVED)
              .certification(certification)
              .build();

      driverRepository.save(driver);

      System.out.println("=== DRIVER USER CREATED ===");
      System.out.println("Email: driver@boozebuddies.com");
      System.out.println("Password: password");
      System.out.println("Role: DRIVER");
      System.out.println("Certification Status: APPROVED");
      System.out.println("Vehicle: Car (DRV-001)");
      System.out.println("==========================");
    } else {
      System.out.println("Driver user already exists");
    }
  }
}
