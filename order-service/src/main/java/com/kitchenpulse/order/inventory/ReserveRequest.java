package com.kitchenpulse.order.inventory;

import java.util.List;
import java.util.UUID;

public record ReserveRequest(UUID orderId, List<ReserveLine> lines) {

	public record ReserveLine(String sku, int quantity) {
	}
}
