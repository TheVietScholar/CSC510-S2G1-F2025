package com.boozebuddies.dto;

import java.time.LocalDateTime;

public class RatingDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String targetType; // "MERCHANT", "DRIVER", "PRODUCT"
    private Long targetId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    // Constructors
    public RatingDTO() {}
    
    public RatingDTO(Long userId, String targetType, Long targetId, Integer rating, String comment) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}