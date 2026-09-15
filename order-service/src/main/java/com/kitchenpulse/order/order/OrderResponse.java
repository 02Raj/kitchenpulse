package com.kitchenpulse.order.order;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(UUID id, String status, String station, String itemsJson, Instant createdAt) {

	public static OrderResponse from(KitchenOrder order) {
		return new OrderResponse(order.getId(), order.getStatus(), order.getStation(), order.getItemsJson(),
				order.getCreatedAt());
	}
}
