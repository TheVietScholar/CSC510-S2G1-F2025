package com.boozebuddies.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Certification {
  @Column(name = "certification_number")
  private String certificationNumber;

  @Column(name = "certification_type")
  private String certificationType;

  @Column(name = "issue_date")
  private LocalDate issueDate;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;

  @Builder.Default private boolean valid = true;

  public boolean isValid() {
    return valid && (expiryDate == null || expiryDate.isAfter(LocalDate.now()));
  }
}
