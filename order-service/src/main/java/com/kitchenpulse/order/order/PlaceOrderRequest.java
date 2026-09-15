package com.kitchenpulse.order.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PlaceOrderRequest(
		@NotBlank String station,
		@NotEmpty List<@Valid OrderLineRequest> items) {

	public record OrderLineRequest(@NotBlank String sku, @Min(1) int quantity, @NotBlank String name) {
	}
}
