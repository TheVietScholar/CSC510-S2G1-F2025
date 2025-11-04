package com.boozebuddies.controller;

import com.boozebuddies.dto.RatingDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.Rating;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.RatingMapper;
import com.boozebuddies.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingControllerTest {

    @Mock
    private RatingService ratingService;

    @Mock
    private RatingMapper ratingMapper;

    @InjectMocks
    private RatingController ratingController;

    private Rating testRating;
    private RatingDTO testRatingDTO;

    @BeforeEach
    void setUp() {
        testRating = Rating.builder()
            .id(1L)
            .rating(5)
            .review("Great service!")
            .build();

        testRatingDTO = RatingDTO.builder()
            .id(1L)
            .rating(5)
            .review("Great service!")
            .build();
    }

    @Test
    void testRateProduct_ValidInput_ReturnsCreatedRating() {
        when(ratingService.rateProduct(any(User.class), any(Product.class), anyInt(), anyString()))
            .thenReturn(testRating);
        when(ratingMapper.toDTO(testRating)).thenReturn(testRatingDTO);

        ResponseEntity<RatingDTO> response = ratingController.rateProduct(1L, 10L, 5, "Excellent product!");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Great service!", response.getBody().getReview());
        
        verify(ratingService, times(1)).rateProduct(any(User.class), any(Product.class), eq(5), eq("Excellent product!"));
        verify(ratingMapper, times(1)).toDTO(testRating);
    }

    @Test
void testRateProduct_NoReview_ReturnsCreatedRating() {
    when(ratingService.rateProduct(any(User.class), any(Product.class), anyInt(), eq(null)))
        .thenReturn(testRating);
    when(ratingMapper.toDTO(testRating)).thenReturn(testRatingDTO);

    ResponseEntity<RatingDTO> response = ratingController.rateProduct(1L, 10L, 4, null);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    
    verify(ratingService, times(1)).rateProduct(any(User.class), any(Product.class), eq(4), eq(null));
}

    @Test
    void testRateDriver_ValidInput_ReturnsCreatedRating() {
        when(ratingService.rateDriver(any(User.class), any(Driver.class), anyInt(), anyString()))
            .thenReturn(testRating);
        when(ratingMapper.toDTO(testRating)).thenReturn(testRatingDTO);

        ResponseEntity<RatingDTO> response = ratingController.rateDriver(1L, 20L, 5, "Great driver!");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        
        verify(ratingService, times(1)).rateDriver(any(User.class), any(Driver.class), eq(5), eq("Great driver!"));
        verify(ratingMapper, times(1)).toDTO(testRating);
    }

    @Test
    void testRateMerchant_ValidInput_ReturnsCreatedRating() {
        when(ratingService.rateMerchant(any(User.class), any(Merchant.class), anyInt(), anyString()))
            .thenReturn(testRating);
        when(ratingMapper.toDTO(testRating)).thenReturn(testRatingDTO);

        ResponseEntity<RatingDTO> response = ratingController.rateMerchant(1L, 30L, 4, "Good merchant");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        
        verify(ratingService, times(1)).rateMerchant(any(User.class), any(Merchant.class), eq(4), eq("Good merchant"));
        verify(ratingMapper, times(1)).toDTO(testRating);
    }

    @Test
    void testGetAverageRatingForProduct_ValidProduct_ReturnsAverage() {
        when(ratingService.getAverageRatingForProduct(any(Product.class))).thenReturn(4.5);

        ResponseEntity<Double> response = ratingController.getAverageRatingForProduct(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4.5, response.getBody());
        
        verify(ratingService, times(1)).getAverageRatingForProduct(any(Product.class));
    }

    @Test
    void testGetAverageRatingForProduct_ZeroAverage_ReturnsZero() {
        when(ratingService.getAverageRatingForProduct(any(Product.class))).thenReturn(0.0);

        ResponseEntity<Double> response = ratingController.getAverageRatingForProduct(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody());
        
        verify(ratingService, times(1)).getAverageRatingForProduct(any(Product.class));
    }

    @Test
    void testGetAverageRatingForDriver_ValidDriver_ReturnsAverage() {
        when(ratingService.getAverageRatingForDriver(any(Driver.class))).thenReturn(4.8);

        ResponseEntity<Double> response = ratingController.getAverageRatingForDriver(20L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4.8, response.getBody());
        
        verify(ratingService, times(1)).getAverageRatingForDriver(any(Driver.class));
    }

    @Test
    void testGetAverageRatingForMerchant_ValidMerchant_ReturnsAverage() {
        when(ratingService.getAverageRatingForMerchant(any(Merchant.class))).thenReturn(3.7);

        ResponseEntity<Double> response = ratingController.getAverageRatingForMerchant(30L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3.7, response.getBody());
        
        verify(ratingService, times(1)).getAverageRatingForMerchant(any(Merchant.class));
    }

    @Test
    void testRateProduct_ServiceThrowsException_PropagatesException() {
        when(ratingService.rateProduct(any(User.class), any(Product.class), anyInt(), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid rating value"));

        assertThrows(IllegalArgumentException.class, () -> {
            ratingController.rateProduct(1L, 10L, 6, "Invalid rating");
        });

        verify(ratingService, times(1)).rateProduct(any(User.class), any(Product.class), eq(6), eq("Invalid rating"));
    }

    @Test
    void testRateDriver_ServiceThrowsException_PropagatesException() {
        when(ratingService.rateDriver(any(User.class), any(Driver.class), anyInt(), anyString()))
            .thenThrow(new IllegalArgumentException("Invalid user"));

        assertThrows(IllegalArgumentException.class, () -> {
            ratingController.rateDriver(1L, 20L, 5, "Test review");
        });

        verify(ratingService, times(1)).rateDriver(any(User.class), any(Driver.class), eq(5), eq("Test review"));
    }

    @Test
    void testControllerCreatesCorrectEntities() {
        when(ratingService.rateProduct(any(User.class), any(Product.class), anyInt(), anyString()))
            .thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                Product product = invocation.getArgument(1);
                assertEquals(1L, user.getId());
                assertEquals(10L, product.getId());
                return testRating;
            });
        when(ratingMapper.toDTO(testRating)).thenReturn(testRatingDTO);

        ratingController.rateProduct(1L, 10L, 5, "Test");

        verify(ratingService, times(1)).rateProduct(any(User.class), any(Product.class), eq(5), eq("Test"));
    }
}