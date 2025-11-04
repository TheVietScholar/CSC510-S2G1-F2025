package com.boozebuddies.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.dto.RegisterUserRequest;
import com.boozebuddies.dto.UserDTO;
import com.boozebuddies.entity.User;
import com.boozebuddies.model.Role;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  private UserMapper userMapper;
  private User testUser;
  private UserDTO testUserDTO;
  private RegisterUserRequest registerRequest;

  @BeforeEach
  void setUp() {
    userMapper = new UserMapper();

    Set<Role> roles = new HashSet<>();
    roles.add(Role.USER);

    testUser =
        User.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .phone("555-123-4567")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .ageVerified(true)
            .isActive(true)
            .isEmailVerified(true)
            .roles(roles)
            .build();

    testUserDTO = new UserDTO();
    testUserDTO.setId(1L);
    testUserDTO.setName("John Doe");
    testUserDTO.setEmail("john@example.com");
    testUserDTO.setPhone("555-123-4567");
    testUserDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
    testUserDTO.setAgeVerified(true);
    testUserDTO.setActive(true);
    testUserDTO.setEmailVerified(true);
    testUserDTO.setRoles(roles);

    registerRequest = new RegisterUserRequest();
    registerRequest.setName("Jane Doe");
    registerRequest.setEmail("jane@example.com");
    registerRequest.setPhone("555-987-6543");
    registerRequest.setDateOfBirth(LocalDate.of(1995, 5, 15));
    registerRequest.setPassword("Password123");
  }

  @Test
  void testToDTO() {
    UserDTO dto = userMapper.toDTO(testUser);

    assertNotNull(dto);
    assertEquals(testUser.getId(), dto.getId());
    assertEquals(testUser.getName(), dto.getName());
    assertEquals(testUser.getEmail(), dto.getEmail());
    assertEquals(testUser.getPhone(), dto.getPhone());
    assertEquals(testUser.getDateOfBirth(), dto.getDateOfBirth());
    assertEquals(testUser.isAgeVerified(), dto.isAgeVerified());
    assertEquals(testUser.isActive(), dto.isActive());
    assertEquals(testUser.isEmailVerified(), dto.isEmailVerified());
    assertEquals(testUser.getRoles(), dto.getRoles());
  }

  @Test
  void testToEntityFromDTO() {
    User entity = userMapper.toEntity(testUserDTO);

    assertNotNull(entity);
    assertEquals(testUserDTO.getId(), entity.getId());
    assertEquals(testUserDTO.getName(), entity.getName());
    assertEquals(testUserDTO.getEmail(), entity.getEmail());
    assertEquals(testUserDTO.getPhone(), entity.getPhone());
    assertEquals(testUserDTO.getDateOfBirth(), entity.getDateOfBirth());
    assertEquals(testUserDTO.isAgeVerified(), entity.isAgeVerified());
    assertEquals(testUserDTO.isActive(), entity.isActive());
    assertEquals(testUserDTO.isEmailVerified(), entity.isEmailVerified());
    assertEquals(testUserDTO.getRoles(), entity.getRoles());
  }

  @Test
  void testToEntityFromRegisterRequest() {
    User entity = userMapper.toEntity(registerRequest);

    assertNotNull(entity);
    assertNull(entity.getId()); // ID should be null for new user
    assertEquals(registerRequest.getName(), entity.getName());
    assertEquals(registerRequest.getEmail(), entity.getEmail());
    assertEquals(registerRequest.getPhone(), entity.getPhone());
    assertEquals(registerRequest.getDateOfBirth(), entity.getDateOfBirth());
    assertEquals(registerRequest.getPassword(), entity.getPasswordHash());
    assertFalse(entity.isAgeVerified());
    assertTrue(entity.isActive());
    assertFalse(entity.isEmailVerified());
    assertNotNull(entity.getRoles());
    assertTrue(entity.getRoles().isEmpty());
  }
}
