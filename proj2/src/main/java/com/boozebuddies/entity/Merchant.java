package com.boozebuddies.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "merchants")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String address;
    
    private String phone;
    
    private String email;
    
    @Column(name = "cuisine_type")
    private String cuisineType;
    
    @Column(name = "opening_time")
    private LocalTime openingTime;
    
    @Column(name = "closing_time")
    private LocalTime closingTime;
    
    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Builder.Default
    private Double rating = 0.0;
    
    @Builder.Default
    @Column(name = "total_ratings")
    private Integer totalRatings = 0;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();
    
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
    
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();
    
    public boolean isOpen() {
        LocalTime now = LocalTime.now();
        return isActive && now.isAfter(openingTime) && now.isBefore(closingTime);
    }
}