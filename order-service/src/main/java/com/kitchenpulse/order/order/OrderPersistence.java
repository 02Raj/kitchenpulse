package com.kitchenpulse.order.order;

import com.kitchenpulse.order.outbox.OutboxEvent;
import com.kitchenpulse.order.outbox.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderPersistence {

	private final KitchenOrderRepository orders;
	private final OutboxEventRepository outbox;
	private final String orderCreatedTopic;

	public OrderPersistence(
			KitchenOrderRepository orders,
			OutboxEventRepository outbox,
			@Value("${kitchenpulse.kafka.order-created-topic}") String orderCreatedTopic) {
		this.orders = orders;
		this.outbox = outbox;
		this.orderCreatedTopic = orderCreatedTopic;
	}

	@Transactional
	public KitchenOrder saveAccepted(UUID orderId, UUID userId, String idempotencyKey, String station, String itemsJson,
			String payload) {
		KitchenOrder order = new KitchenOrder(orderId, userId, idempotencyKey, "ACCEPTED", station, itemsJson,
				Instant.now());
		orders.save(order);
		outbox.save(new OutboxEvent(UUID.randomUUID(), orderId, orderCreatedTopic, payload, Instant.now()));
		return order;
	}
}
