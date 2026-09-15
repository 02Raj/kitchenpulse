package com.kitchenpulse.notify.ticket;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("kitchen_tickets")
public class KitchenTicket {

	@Id
	private String id;

	@Indexed(unique = true)
	private String orderId;

	private String station;
	private String itemsJson;
	private String status;
	private Instant createdAt;

	protected KitchenTicket() {
	}

	public KitchenTicket(String orderId, String station, String itemsJson, String status, Instant createdAt) {
		this.orderId = orderId;
		this.station = station;
		this.itemsJson = itemsJson;
		this.status = status;
		this.createdAt = createdAt;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getStation() {
		return station;
	}

	public String getItemsJson() {
		return itemsJson;
	}

	public String getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void bump() {
		this.status = "NEW";
	}
}
