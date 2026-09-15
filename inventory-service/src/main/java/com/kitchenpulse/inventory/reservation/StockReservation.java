package com.kitchenpulse.inventory.reservation;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document("reservations")
public class StockReservation {

	@Id
	private String id;

	@Indexed(unique = true)
	private UUID orderId;

	private List<Line> lines;
	private Instant createdAt;

	protected StockReservation() {
	}

	public StockReservation(UUID orderId, List<Line> lines, Instant createdAt) {
		this.orderId = orderId;
		this.lines = lines;
		this.createdAt = createdAt;
	}

	public record Line(String sku, int quantity) {
	}
}
