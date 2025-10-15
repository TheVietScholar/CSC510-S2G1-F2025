package com.boozebuddies.merchant;

public class Merchant {
	private Long merchantId;
	private String name;
	private MerchantType type;
	private Address address;

	// Constructor
	public Merchant(Long merchantId, String name, MerchantType type, Address address) {
		this.merchantId = merchantId;
		this.name = name;
		this.type = type;
		this.address = address;
	}

	// Getters and Setters
	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MerchantType getType() {
		return type;
	}

	public void setType(MerchantType type) {
		this.type = type;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
}
