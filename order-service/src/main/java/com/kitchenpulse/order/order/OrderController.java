package com.kitchenpulse.order.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final PlaceOrderService placeOrderService;

	public OrderController(PlaceOrderService placeOrderService) {
		this.placeOrderService = placeOrderService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponse place(
			@RequestHeader(name = "Idempotency-Key") String idempotencyKey,
			@Valid @RequestBody PlaceOrderRequest request) {
		return placeOrderService.place(idempotencyKey, request);
	}

	@GetMapping
	public List<OrderResponse> mine() {
		return placeOrderService.mine();
	}
}
