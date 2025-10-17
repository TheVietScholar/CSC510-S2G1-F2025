package com.boozebuddies.dto;

import java.time.LocalDateTime;

public class DriverDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String vehicleType;
    private String licensePlate;
    private boolean isAvailable;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double rating;
    private Integer totalDeliveries;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public DriverDTO() {}
    
    public DriverDTO(Long id, String name, String email, String phone, String vehicleType, String licensePlate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.vehicleType = vehicleType;
        this.licensePlate = licensePlate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }

    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(Integer totalDeliveries) { this.totalDeliveries = totalDeliveries; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}