package com.kitchenpulse.order.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPublisher {

	private final OutboxEventRepository outbox;
	private final KafkaTemplate<String, String> kafkaTemplate;

	public OutboxPublisher(OutboxEventRepository outbox, KafkaTemplate<String, String> kafkaTemplate) {
		this.outbox = outbox;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Scheduled(fixedDelay = 2000)
	@Transactional
	public void publishBatch() {
		List<OutboxEvent> pending = outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
		for (OutboxEvent event : pending) {
			kafkaTemplate.send(event.getTopic(), event.getId().toString(), event.getPayload());
			event.markPublished(Instant.now());
		}
	}
}
