package com.boozebuddies.entity;

import lombok.*;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

import jakarta.persistence.Column;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CertificationsStatus {
    @Column(name = "certification_number")
    private String certificationNumber;
    
    @Column(name = "certification_type")
    private String certificationType;
    
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Builder.Default
    private boolean valid = true;
    
    public boolean isValid() {
        return valid && (expiryDate == null || expiryDate.isAfter(LocalDate.now()));
    }
}