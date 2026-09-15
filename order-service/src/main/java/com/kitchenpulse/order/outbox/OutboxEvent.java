package com.kitchenpulse.order.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

	@Id
	private UUID id;

	@Column(name = "aggregate_id", nullable = false)
	private UUID aggregateId;

	@Column(nullable = false)
	private String topic;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false)
	private String payload;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected OutboxEvent() {
	}

	public OutboxEvent(UUID id, UUID aggregateId, String topic, String payload, Instant createdAt) {
		this.id = id;
		this.aggregateId = aggregateId;
		this.topic = topic;
		this.payload = payload;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public String getTopic() {
		return topic;
	}

	public String getPayload() {
		return payload;
	}

	public void markPublished(Instant at) {
		this.publishedAt = at;
	}
}
