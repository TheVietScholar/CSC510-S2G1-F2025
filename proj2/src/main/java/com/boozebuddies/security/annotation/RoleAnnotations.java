package com.boozebuddies.security.annotation;

import java.lang.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * A collection of custom Spring Security annotations that simplify role-based access control within
 * controller classes.
 *
 * <p>These annotations wrap commonly used {@link PreAuthorize} expressions, allowing for cleaner
 * and more readable method-level security declarations.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/users")
 * public class UserController {
 *
 *     @RoleAnnotations.IsAdmin
 *     @DeleteMapping("/{id}")
 *     public ResponseEntity<?> deleteUser(@PathVariable Long id) {
 *         // Only admins can delete users
 *         ...
 *     }
 * }
 * }</pre>
 */
public class RoleAnnotations {

  /**
   * Restricts access to users with the {@code USER} role.
   *
   * <p>Applies to controller methods or classes that should only be available to regular
   * authenticated users.
   *
   * <p>Equivalent to: {@code @PreAuthorize("hasRole('USER')")}
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('USER')")
  public @interface IsUser {}

  /**
   * Restricts access to users with the {@code ADMIN} role.
   *
   * <p>Use this for administrative operations that should not be accessible to non-admin users.
   *
   * <p>Equivalent to: {@code @PreAuthorize("hasRole('ADMIN')")}
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('ADMIN')")
  public @interface IsAdmin {}

  /**
   * Restricts access to users with the {@code MERCHANT_ADMIN} role.
   *
   * <p>Intended for endpoints that manage or monitor merchant-related functionality.
   *
   * <p>Equivalent to: {@code @PreAuthorize("hasRole('MERCHANT_ADMIN')")}
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('MERCHANT_ADMIN')")
  public @interface IsMerchantAdmin {}

  /**
   * Restricts access to users with the {@code DRIVER} role.
   *
   * <p>Applies to endpoints or operations intended only for delivery drivers.
   *
   * <p>Equivalent to: {@code @PreAuthorize("hasRole('DRIVER')")}
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('DRIVER')")
  public @interface IsDriver {}

  /**
   * Restricts access to users with either the {@code ADMIN} or {@code MERCHANT_ADMIN} role.
   *
   * <p>Useful for endpoints shared between system administrators and merchant administrators.
   *
   * <p>Equivalent to: {@code @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT_ADMIN')")}
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT_ADMIN')")
  public @interface IsAdminOrMerchantAdmin {}

  /**
   * Restricts access to either an {@code ADMIN} or the authenticated user acting on their own
   * resource.
   *
   * <p>Uses {@code @permissionService.isSelf(authentication, #id)} to verify ownership of the
   * resource being accessed.
   *
   * <p>Usage example:
   *
   * <pre>{@code
   * @GetMapping("/users/{id}")
   * @IsSelfOrAdmin(idParam = "id")
   * public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
   *     ...
   * }
   * }</pre>
   *
   * <p>Equivalent to: {@code @PreAuthorize("hasRole('ADMIN')
   * or @permissionService.isSelf(authentication, #id)")}
   */
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('ADMIN') or @permissionService.isSelf(authentication, #id)")
  public @interface IsSelfOrAdmin {
    /**
     * The name of the method parameter representing the user ID. Defaults to {@code "id"}.
     *
     * @return the parameter name to evaluate
     */
    String idParam() default "id";
  }

  /**
   * Restricts access to either an {@code ADMIN} or a {@code MERCHANT_ADMIN} who owns the specified
   * merchant.
   *
   * <p>Uses {@code @permissionService.ownsMerchant(authentication, #merchantId)} to verify
   * ownership.
   *
   * <p>Usage example:
   *
   * <pre>{@code
   * @PutMapping("/merchants/{merchantId}")
   * @OwnsMerchantOrAdmin(merchantIdParam = "merchantId")
   * public ResponseEntity<?> updateMerchant(@PathVariable Long merchantId) {
   *     ...
   * }
   * }</pre>
   *
   * <p>Equivalent to: {@code @PreAuthorize("hasRole('ADMIN')
   * or @permissionService.ownsMerchant(authentication, #merchantId)")}
   */
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('ADMIN') or @permissionService.ownsMerchant(authentication, #merchantId)")
  public @interface OwnsMerchantOrAdmin {
    /**
     * The name of the method parameter representing the merchant ID. Defaults to {@code
     * "merchantId"}.
     *
     * @return the parameter name to evaluate
     */
    String merchantIdParam() default "merchantId";
  }

  /**
   * Restricts access to any authenticated user, regardless of role.
   *
   * <p>Useful for endpoints that only require login but not specific roles.
   *
   * <p>Equivalent to: {@code @PreAuthorize("isAuthenticated()")}
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("isAuthenticated()")
  public @interface IsAuthenticated {}
}
