package com.kitchenpulse.notify.ticket;

import java.time.Instant;

public record KitchenTicketResponse(String orderId, String station, String itemsJson, String status, Instant createdAt) {

	public static KitchenTicketResponse from(KitchenTicket ticket) {
		return new KitchenTicketResponse(ticket.getOrderId(), ticket.getStation(), ticket.getItemsJson(),
				ticket.getStatus(), ticket.getCreatedAt());
	}
}
