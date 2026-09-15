package com.kitchenpulse.order.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchenpulse.order.common.ApiException;
import com.kitchenpulse.order.common.SecurityUtils;
import com.kitchenpulse.order.inventory.InventoryGateway;
import com.kitchenpulse.order.inventory.ReserveRequest;
import com.kitchenpulse.order.inventory.ReserveResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlaceOrderService {

	private final KitchenOrderRepository orders;
	private final OrderPersistence persistence;
	private final InventoryGateway inventory;
	private final ObjectMapper objectMapper;

	public PlaceOrderService(
			KitchenOrderRepository orders,
			OrderPersistence persistence,
			InventoryGateway inventory,
			ObjectMapper objectMapper) {
		this.orders = orders;
		this.persistence = persistence;
		this.inventory = inventory;
		this.objectMapper = objectMapper;
	}

	public OrderResponse place(String idempotencyKey, PlaceOrderRequest request) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required");
		}
		return orders.findByIdempotencyKey(idempotencyKey)
				.map(OrderResponse::from)
				.orElseGet(() -> create(idempotencyKey, request));
	}

	public List<OrderResponse> mine() {
		return orders.findByUserIdOrderByCreatedAtDesc(SecurityUtils.currentUserId()).stream()
				.map(OrderResponse::from)
				.toList();
	}

	private OrderResponse create(String idempotencyKey, PlaceOrderRequest request) {
		UUID orderId = UUID.randomUUID();
		UUID userId = SecurityUtils.currentUserId();
		List<ReserveRequest.ReserveLine> lines = request.items().stream()
				.map(item -> new ReserveRequest.ReserveLine(item.sku(), item.quantity()))
				.toList();
		ReserveResponse reservation = inventory.reserve(new ReserveRequest(orderId, lines));
		if (!reservation.reserved()) {
			throw new ApiException(HttpStatus.CONFLICT,
					reservation.reason() == null ? "Insufficient stock" : reservation.reason());
		}
		String itemsJson = writeJson(request.items());
		String payload = writeJson(Map.of(
				"eventId", UUID.randomUUID().toString(),
				"orderId", orderId.toString(),
				"station", request.station(),
				"items", request.items(),
				"occurredAt", Instant.now().toString()));
		return OrderResponse.from(
				persistence.saveAccepted(orderId, userId, idempotencyKey, request.station(), itemsJson, payload));
	}

	private String writeJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}
}
