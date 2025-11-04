package com.boozebuddies.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.User;
import com.boozebuddies.exception.UnauthorizedException;
import com.boozebuddies.exception.ValidationException;
import com.boozebuddies.model.Role;
import com.boozebuddies.service.MerchantService;
import com.boozebuddies.service.UserService;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

  @Mock private UserService userService;
  @Mock private MerchantService merchantService;

  @InjectMocks private RoleServiceImpl roleService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setRoles(new HashSet<>());
  }

  @Test
  void testAssignRole_ValidRole_Success() {
    when(userService.findById(1L)).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(i -> i.getArgument(1));

    User result = roleService.assignRole(1L, Role.USER);

    assertTrue(result.getRoles().contains(Role.USER));
    verify(userService).updateUser(1L, testUser);
  }

  @Test
  void testAssignRole_MerchantAdminAndDriverConflict_ThrowsException() {
    testUser.addRole(Role.DRIVER);
    when(userService.findById(1L)).thenReturn(testUser);

    assertThrows(ValidationException.class, () -> roleService.assignRole(1L, Role.MERCHANT_ADMIN));
    verify(userService, never()).updateUser(any(), any());
  }

  @Test
  void testAssignRole_DriverAndMerchantConflict_ThrowsException() {
    testUser.addRole(Role.MERCHANT_ADMIN);
    when(userService.findById(1L)).thenReturn(testUser);

    assertThrows(ValidationException.class, () -> roleService.assignRole(1L, Role.DRIVER));
  }

  @Test
  void testAssignRole_DriverNotAgeVerified_ThrowsException() {
    testUser.setAgeVerified(false);
    when(userService.findById(1L)).thenReturn(testUser);

    assertThrows(ValidationException.class, () -> roleService.assignRole(1L, Role.DRIVER));
  }

  @Test
  void testAssignRoleWithMerchant_ValidMerchantAdmin_Success() {
    when(userService.findById(1L)).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(i -> i.getArgument(1));

    User result = roleService.assignRoleWithMerchant(1L, Role.MERCHANT_ADMIN, 10L);

    assertEquals(10L, result.getMerchantId());
    assertTrue(result.getRoles().contains(Role.MERCHANT_ADMIN));
    verify(userService).updateUser(1L, testUser);
  }

  @Test
  void testAssignRoleWithMerchant_NullMerchantId_ThrowsValidationException() {
    when(userService.findById(1L)).thenReturn(testUser);

    assertThrows(
        ValidationException.class,
        () -> roleService.assignRoleWithMerchant(1L, Role.MERCHANT_ADMIN, null));
  }

  @Test
  void testRemoveRole_ValidRole_RemovesSuccessfully() {
    testUser.addRole(Role.USER);
    testUser.addRole(Role.DRIVER);
    when(userService.findById(1L)).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(i -> i.getArgument(1));

    User result = roleService.removeRole(1L, Role.DRIVER);

    assertFalse(result.getRoles().contains(Role.DRIVER));
    verify(userService).updateUser(1L, testUser);
  }

  @Test
  void testRemoveRole_LastRole_ThrowsException() {
    testUser.addRole(Role.USER);
    when(userService.findById(1L)).thenReturn(testUser);

    assertThrows(ValidationException.class, () -> roleService.removeRole(1L, Role.USER));
  }

  @Test
  void testRemoveRole_RemovingMerchantAdmin_ClearsMerchantId() {
    testUser.addRole(Role.MERCHANT_ADMIN);
    testUser.addRole(Role.USER); // ✅ Add an extra role so it's not the last one
    testUser.setMerchantId(5L);

    when(userService.findById(1L)).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(i -> i.getArgument(1));

    User result = roleService.removeRole(1L, Role.MERCHANT_ADMIN);

    assertNull(result.getMerchantId());
    assertFalse(result.getRoles().contains(Role.MERCHANT_ADMIN));
    verify(userService).updateUser(eq(1L), any(User.class));
  }

  @Test
  void testSetRoles_ValidRoles_Success() {
    Set<Role> roles = new HashSet<>();
    roles.add(Role.ADMIN);
    when(userService.findById(1L)).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(i -> i.getArgument(1));

    User result = roleService.setRoles(1L, roles);

    assertTrue(result.getRoles().contains(Role.ADMIN));
    verify(userService).updateUser(1L, testUser);
  }

  @Test
  void testSetRoles_EmptyRoles_ThrowsException() {
    assertThrows(ValidationException.class, () -> roleService.setRoles(1L, new HashSet<>()));
  }

  @Test
  void testSetRoles_RemovesMerchantId_WhenMerchantAdminMissing() {
    testUser.setMerchantId(100L);
    Set<Role> roles = new HashSet<>();
    roles.add(Role.USER);
    when(userService.findById(1L)).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(i -> i.getArgument(1));

    User result = roleService.setRoles(1L, roles);

    assertNull(result.getMerchantId());
  }

  @Test
  void testAssignMerchantToUser_ValidCase_Success() {
    testUser.addRole(Role.MERCHANT_ADMIN);
    Merchant merchant = new Merchant();
    merchant.setId(5L);
    when(userService.findById(1L)).thenReturn(testUser);
    when(merchantService.getMerchantById(5L)).thenReturn(merchant);
    when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(i -> i.getArgument(1));

    User result = roleService.assignMerchantToUser(1L, 5L);

    assertEquals(5L, result.getMerchantId());
    verify(userService).updateUser(1L, testUser);
  }

  @Test
  void testAssignMerchantToUser_UserNotMerchantAdmin_ThrowsUnauthorized() {
    when(userService.findById(1L)).thenReturn(testUser);

    assertThrows(UnauthorizedException.class, () -> roleService.assignMerchantToUser(1L, 10L));
  }

  @Test
  void testAssignMerchantToUser_NullMerchantId_ThrowsValidation() {
    testUser.addRole(Role.MERCHANT_ADMIN);
    when(userService.findById(1L)).thenReturn(testUser);

    assertThrows(ValidationException.class, () -> roleService.assignMerchantToUser(1L, null));
  }

  @Test
  void testAssignMerchantToUser_MerchantNotFound_ThrowsValidation() {
    testUser.addRole(Role.MERCHANT_ADMIN);
    when(userService.findById(1L)).thenReturn(testUser);
    when(merchantService.getMerchantById(99L)).thenReturn(null);

    assertThrows(ValidationException.class, () -> roleService.assignMerchantToUser(1L, 99L));
  }

  @Test
  void testRemoveMerchantFromUser_SetsMerchantIdNull() {
    testUser.setMerchantId(50L);
    when(userService.findById(1L)).thenReturn(testUser);
    when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(i -> i.getArgument(1));

    User result = roleService.removeMerchantFromUser(1L);

    assertNull(result.getMerchantId());
    verify(userService).updateUser(1L, testUser);
  }

  @Test
  void testCanAccessMerchant_Admin_ReturnsTrue() {
    testUser.addRole(Role.ADMIN);
    assertTrue(roleService.canAccessMerchant(testUser, 10L));
  }

  @Test
  void testCanAccessMerchant_MerchantAdminOwnsMerchant_ReturnsTrue() {
    testUser.addRole(Role.MERCHANT_ADMIN);
    testUser.setMerchantId(10L);
    assertTrue(roleService.canAccessMerchant(testUser, 10L));
  }

  @Test
  void testCanAccessMerchant_MerchantAdminDifferentMerchant_ReturnsFalse() {
    testUser.addRole(Role.MERCHANT_ADMIN);
    testUser.setMerchantId(5L);
    assertFalse(roleService.canAccessMerchant(testUser, 10L));
  }

  @Test
  void testCanAccessMerchant_RegularUser_ReturnsFalse() {
    testUser.addRole(Role.USER);
    assertFalse(roleService.canAccessMerchant(testUser, 10L));
  }

  @Test
  void testGetPrimaryRole_Admin() {
    testUser.addRole(Role.ADMIN);
    assertEquals(Role.ADMIN, roleService.getPrimaryRole(testUser));
  }

  @Test
  void testGetPrimaryRole_MerchantAdmin() {
    testUser.addRole(Role.MERCHANT_ADMIN);
    assertEquals(Role.MERCHANT_ADMIN, roleService.getPrimaryRole(testUser));
  }

  @Test
  void testGetPrimaryRole_Driver() {
    testUser.addRole(Role.DRIVER);
    assertEquals(Role.DRIVER, roleService.getPrimaryRole(testUser));
  }

  @Test
  void testGetPrimaryRole_DefaultUser() {
    testUser.addRole(Role.USER);
    assertEquals(Role.USER, roleService.getPrimaryRole(testUser));
  }
}
