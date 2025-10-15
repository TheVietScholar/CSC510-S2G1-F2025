public class Driver {

	private Long driverId;
	private CertificationStatus certificationStatus;
	private boolean isAvailable;

	public enum CertificationStatus {
		CERTIFIED, PENDING, EXPIRED
	}

	// Constructor
	public Driver(Long driverId, CertificationStatus certificationStatus, boolean isAvailable) {
		this.driverId = driverId;
		this.certificationStatus = certificationStatus;
		this.isAvailable = isAvailable;
	}

	public Long getDriverId() {
		return driverId;
	}

	public void setDriverId(Long driverId) {
		this.driverId = driverId;
	}

	public CertificationStatus getCertificationStatus() {
		return certificationStatus;
	}

	public void setCertificationStatus(CertificationStatus certificationStatus) {
		this.certificationStatus = certificationStatus;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean available) {
		isAvailable = available;
	}

	// Methods
	public boolean verifyCertification() {
		// Example logic: return true if certified
		return certificationStatus == CertificationStatus.CERTIFIED;
	}

	public void updateLocation(double latitude, double longitude) {
		// Placeholder: You can implement GPS tracking here
		System.out.println("Driver location updated to: " + latitude + ", " + longitude);
	}
}
