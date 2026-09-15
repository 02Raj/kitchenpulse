package com.kitchenpulse.order.inventory;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventoryClient", url = "${kitchenpulse.inventory.base-url}")
public interface InventoryClient {

	@PostMapping("/internal/reservations")
	ReserveResponse reserve(@RequestBody ReserveRequest request);
}
