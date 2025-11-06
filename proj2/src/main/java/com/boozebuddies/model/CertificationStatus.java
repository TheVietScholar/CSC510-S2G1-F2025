package com.boozebuddies.model;

/** Enum representing the certification status of a driver. */
public enum CertificationStatus {
  /** Driver has applied but not yet approved */
  PENDING,

  /** Driver is certified and approved */
  APPROVED,

  /** Certification has been revoked */
  REVOKED
}
