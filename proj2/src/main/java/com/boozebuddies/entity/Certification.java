package com.boozebuddies.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.*;

/** Embeddable entity representing driver certification information. */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Certification {
  /** The certification number */
  @Column(name = "certification_number")
  private String certificationNumber;

  /** The type of certification */
  @Column(name = "certification_type")
  private String certificationType;

  /** When the certification was issued */
  @Column(name = "issue_date")
  private LocalDate issueDate;

  /** When the certification expires */
  @Column(name = "expiry_date")
  private LocalDate expiryDate;

  /** Whether the certification is valid */
  @Builder.Default private boolean valid = true;

  /**
   * Checks if the certification is currently valid.
   *
   * @return true if the certification is valid and not expired, false otherwise
   */
  public boolean isValid() {
    return valid && (expiryDate == null || expiryDate.isAfter(LocalDate.now()));
  }
}
