package com.boozebuddies.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.boozebuddies.dto.DriverDTO;
import com.boozebuddies.entity.Certification;
import com.boozebuddies.entity.Driver;
import com.boozebuddies.model.CertificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DriverMapper Test")
public class DriverMapperTest {

  private DriverMapper driverMapper;
  private Driver testDriver;

  @BeforeEach
  public void setUp() {
    driverMapper = new DriverMapper();
    testDriver =
        Driver.builder()
            .id(1L)
            .name("John Doe")
            .email("example@test.com")
            .phone("123-456-7890")
            .vehicleType("Car")
            .licensePlate("XYZ 1234")
            .isAvailable(true)
            .currentLatitude(37.7749)
            .currentLongitude(-122.4194)
            .rating(4.5)
            .totalDeliveries(100)
            .certificationStatus(CertificationStatus.APPROVED)
            .certification(
                Certification.builder()
                    .certificationNumber("CERT123")
                    .certificationType("Type A")
                    .issueDate(java.time.LocalDate.now().minusYears(1))
                    .expiryDate(java.time.LocalDate.now().plusYears(1))
                    .valid(true)
                    .build())
            .build();
  }

  @Test
  @DisplayName("Test Driver to DriverDTO Mapping")
  public void testDriverToDriverDTO() {
    DriverDTO dto = driverMapper.toDTO(testDriver);

    assertNotNull(testDriver);
    assertEquals(testDriver.getId(), dto.getId(), "ID should match");
    assertEquals(testDriver.getName(), dto.getName(), "Name should match");
    assertEquals(testDriver.getEmail(), dto.getEmail(), "Email should match");
    assertEquals(testDriver.getPhone(), dto.getPhone(), "Phone should match");
    assertEquals(testDriver.getVehicleType(), dto.getVehicleType(), "Vehicle Type should match");
    assertEquals(testDriver.getLicensePlate(), dto.getLicensePlate(), "License Plate should match");
    assertEquals(testDriver.isAvailable(), dto.isAvailable(), "Availability should match");
    assertEquals(
        testDriver.getCurrentLatitude(), dto.getCurrentLatitude(), "Current Latitude should match");
    assertEquals(
        testDriver.getCurrentLongitude(),
        dto.getCurrentLongitude(),
        "Current Longitude should match");
    assertEquals(testDriver.getRating(), dto.getRating(), "Rating should match");
    assertEquals(
        testDriver.getTotalDeliveries(), dto.getTotalDeliveries(), "Total Deliveries should match");
    assertEquals(
        testDriver.getCertificationStatus().name(),
        dto.getCertificationStatus(),
        "Certification Status should match");
    assertEquals(
        testDriver.getCertification().getCertificationNumber(),
        dto.getCertification().getCertificationNumber(),
        "Certification Number should match");
  }

  @Test
  @DisplayName("Test DriverDTO to Driver Mapping")
  public void testDriverDTOToDriver() {
    Driver driver = driverMapper.toEntity(driverMapper.toDTO(testDriver));

    assertNotNull(driver);
    assertEquals(testDriver.getId(), driver.getId(), "ID should match");
    assertEquals(testDriver.getName(), driver.getName(), "Name should match");
    assertEquals(testDriver.getEmail(), driver.getEmail(), "Email should match");
    assertEquals(testDriver.getPhone(), driver.getPhone(), "Phone should match");
    assertEquals(testDriver.getVehicleType(), driver.getVehicleType(), "Vehicle Type should match");
    assertEquals(
        testDriver.getLicensePlate(), driver.getLicensePlate(), "License Plate should match");
    assertEquals(testDriver.isAvailable(), driver.isAvailable(), "Availability should match");
    assertEquals(
        testDriver.getCurrentLatitude(),
        driver.getCurrentLatitude(),
        "Current Latitude should match");
    assertEquals(
        testDriver.getCurrentLongitude(),
        driver.getCurrentLongitude(),
        "Current Longitude should match");
    assertEquals(testDriver.getRating(), driver.getRating(), "Rating should match");
    assertEquals(
        testDriver.getTotalDeliveries(),
        driver.getTotalDeliveries(),
        "Total Deliveries should match");
    assertEquals(
        testDriver.getCertificationStatus(),
        driver.getCertificationStatus(),
        "Certification Status should match");
    assertEquals(
        testDriver.getCertification().getCertificationNumber(),
        driver.getCertification().getCertificationNumber(),
        "Certification Number should match");
  }

  @Test
  @DisplayName("Test Null Driver to DriverDTO Mapping")
  public void testNullDriverToDriverDTO() {
    DriverDTO dto = driverMapper.toDTO(null);
    assertNull(dto, "Mapping null Driver should return null DriverDTO");
  }

  @Test
  @DisplayName("Test Null DriverDTO to Driver Mapping")
  public void testNullDriverDTOToDriver() {
    Driver driver = driverMapper.toEntity(null);
    assertNull(driver, "Mapping null DriverDTO should return null Driver");
  }
}
