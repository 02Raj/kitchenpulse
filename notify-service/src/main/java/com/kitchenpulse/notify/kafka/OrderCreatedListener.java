package com.kitchenpulse.notify.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchenpulse.notify.ticket.KitchenTicket;
import com.kitchenpulse.notify.ticket.KitchenTicketRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderCreatedListener {

	private final KitchenTicketRepository tickets;
	private final ObjectMapper objectMapper;

	public OrderCreatedListener(KitchenTicketRepository tickets, ObjectMapper objectMapper) {
		this.tickets = tickets;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "${kitchenpulse.kafka.order-created-topic}")
	public void onMessage(String payload) throws Exception {
		JsonNode node = objectMapper.readTree(payload);
		String orderId = node.path("orderId").asText();
		if (orderId.isBlank()) {
			throw new IllegalArgumentException("orderId missing");
		}
		tickets.findByOrderId(orderId).ifPresentOrElse(existing -> {
		}, () -> tickets.save(new KitchenTicket(
				orderId,
				node.path("station").asText("GRILL"),
				node.path("items").toString(),
				"NEW",
				Instant.now())));
	}
}
