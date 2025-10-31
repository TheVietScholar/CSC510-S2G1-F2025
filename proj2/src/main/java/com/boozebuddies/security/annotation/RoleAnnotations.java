package com.boozebuddies.security.annotation;

import java.lang.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Custom annotations for role-based access control.
 * These make controller methods cleaner and more readable.
 */
public class RoleAnnotations {

  /**
   * Only users with USER role can access this endpoint.
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('USER')")
  public @interface IsUser {}

  /**
   * Only users with ADMIN role can access this endpoint.
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('ADMIN')")
  public @interface IsAdmin {}

  /**
   * Only users with MERCHANT_ADMIN role can access this endpoint.
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('MERCHANT_ADMIN')")
  public @interface IsMerchantAdmin {}

  /**
   * Only users with DRIVER role can access this endpoint.
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('DRIVER')")
  public @interface IsDriver {}

  /**
   * Users with ADMIN or MERCHANT_ADMIN roles can access this endpoint.
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT_ADMIN')")
  public @interface IsAdminOrMerchantAdmin {}

  /**
   * Check if the authenticated user is accessing their own resource.
   * Usage: @IsSelfOrAdmin(idParam = "id")
   */
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('ADMIN') or @permissionService.isSelf(authentication, #id)")
  public @interface IsSelfOrAdmin {
    String idParam() default "id";
  }

  /**
   * Check if merchant admin owns the merchant they're trying to access.
   * Usage: @OwnsResourceOrAdmin(resourceIdParam = "merchantId")
   */
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("hasRole('ADMIN') or @permissionService.ownsMerchant(authentication, #merchantId)")
  public @interface OwnsMerchantOrAdmin {
    String merchantIdParam() default "merchantId";
  }

  /**
   * Only authenticated users (any role) can access.
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @PreAuthorize("isAuthenticated()")
  public @interface IsAuthenticated {}
}
