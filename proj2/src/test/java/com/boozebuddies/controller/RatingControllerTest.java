package com.boozebuddies.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.boozebuddies.dto.RatingDTO;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.entity.Merchant;
import com.boozebuddies.entity.Product;
import com.boozebuddies.entity.Rating;
import com.boozebuddies.entity.User;
import com.boozebuddies.mapper.RatingMapper;
import com.boozebuddies.model.Role;
import com.boozebuddies.service.PermissionService;
import com.boozebuddies.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class RatingControllerTest {

  @Mock private RatingService ratingService;
  @Mock private RatingMapper ratingMapper;
  @Mock private PermissionService permissionService;
  @Mock private Authentication auth;

  @InjectMocks private RatingController ratingController;

  private Rating testRating;
  private RatingDTO testRatingDTO;

  @BeforeEach
  void setUp() {
    testRating = Rating.builder().id(1L).rating(5).review("Great service!").build();
    testRatingDTO = RatingDTO.builder().id(1L).rating(5).review("Great service!").build();
  }

  @Test
  void testRateProduct_ValidInput_ReturnsCreatedRating() {
    when(permissionService.isSelf(auth, 1L)).thenReturn(true);
    when(ratingService.rateProduct(
            any(User.class), any(Product.class), eq(5), eq("Excellent product!")))
        .thenReturn(testRating);
    when(ratingMapper.toDTO(testRating)).thenReturn(testRatingDTO);

    ResponseEntity<RatingDTO> response =
        ratingController.rateProduct(1L, 10L, 5, "Excellent product!", auth);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    RatingDTO body = response.getBody(); // safe after assertNotNull
    assertEquals(1L, body.getId());
    assertEquals(5, body.getRating());
    assertEquals("Great service!", body.getReview());

    verify(ratingService, times(1))
        .rateProduct(any(User.class), any(Product.class), eq(5), eq("Excellent product!"));
    verify(ratingMapper, times(1)).toDTO(testRating);
  }

  @Test
  void testRateProduct_Forbidden() {
    when(permissionService.isSelf(auth, 1L)).thenReturn(false);
    when(permissionService.hasRole(auth, Role.ADMIN)).thenReturn(false);

    ResponseEntity<RatingDTO> response =
        ratingController.rateProduct(1L, 10L, 5, "Forbidden", auth);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertNull(response.getBody());

    verify(ratingService, never()).rateProduct(any(), any(), anyInt(), anyString());
    verify(ratingMapper, never()).toDTO(any());
  }

  @Test
  void testRateDriver_ValidInput_ReturnsCreatedRating() {
    when(permissionService.isSelf(auth, 1L)).thenReturn(true);
    when(ratingService.rateDriver(any(User.class), any(Driver.class), eq(5), eq("Great driver!")))
        .thenReturn(testRating);
    when(ratingMapper.toDTO(testRating)).thenReturn(testRatingDTO);

    ResponseEntity<RatingDTO> response =
        ratingController.rateDriver(1L, 20L, 5, "Great driver!", auth);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    RatingDTO body = response.getBody();
    assertEquals(1L, body.getId());

    verify(ratingService, times(1))
        .rateDriver(any(User.class), any(Driver.class), eq(5), eq("Great driver!"));
    verify(ratingMapper, times(1)).toDTO(testRating);
  }

  @Test
  void testRateMerchant_ValidInput_ReturnsCreatedRating() {
    when(permissionService.isSelf(auth, 1L)).thenReturn(true);
    when(ratingService.rateMerchant(
            any(User.class), any(Merchant.class), eq(4), eq("Good merchant")))
        .thenReturn(testRating);
    when(ratingMapper.toDTO(testRating)).thenReturn(testRatingDTO);

    ResponseEntity<RatingDTO> response =
        ratingController.rateMerchant(1L, 30L, 4, "Good merchant", auth);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    RatingDTO body = response.getBody();
    assertEquals(1L, body.getId());

    verify(ratingService, times(1))
        .rateMerchant(any(User.class), any(Merchant.class), eq(4), eq("Good merchant"));
    verify(ratingMapper, times(1)).toDTO(testRating);
  }

  @Test
  void testGetAverageRatingForProduct() {
    when(ratingService.getAverageRatingForProduct(any(Product.class))).thenReturn(4.5);

    ResponseEntity<Double> response = ratingController.getAverageRatingForProduct(10L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(4.5, response.getBody());

    verify(ratingService, times(1)).getAverageRatingForProduct(any(Product.class));
  }

  @Test
  void testGetAverageRatingForDriver() {
    when(ratingService.getAverageRatingForDriver(any(Driver.class))).thenReturn(4.8);

    ResponseEntity<Double> response = ratingController.getAverageRatingForDriver(20L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(4.8, response.getBody());

    verify(ratingService, times(1)).getAverageRatingForDriver(any(Driver.class));
  }

  @Test
  void testGetAverageRatingForMerchant() {
    when(ratingService.getAverageRatingForMerchant(any(Merchant.class))).thenReturn(3.7);

    ResponseEntity<Double> response = ratingController.getAverageRatingForMerchant(30L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(3.7, response.getBody());

    verify(ratingService, times(1)).getAverageRatingForMerchant(any(Merchant.class));
  }
}
