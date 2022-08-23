package com.eazybytes.cards.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


public class Customer {

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	private int customerId;

}
