package com.kitchenpulse.order.inventory;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.kitchenpulse.order.common.ApiException;

@Component
public class InventoryGateway {

	private final InventoryClient inventoryClient;

	public InventoryGateway(InventoryClient inventoryClient) {
		this.inventoryClient = inventoryClient;
	}

	@CircuitBreaker(name = "inventory", fallbackMethod = "reserveFallback")
	public ReserveResponse reserve(ReserveRequest request) {
		return inventoryClient.reserve(request);
	}

	@SuppressWarnings("unused")
	ReserveResponse reserveFallback(ReserveRequest request, Throwable error) {
		throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
				"Inventory unavailable. Circuit open or call failed: " + error.getClass().getSimpleName());
	}
}
