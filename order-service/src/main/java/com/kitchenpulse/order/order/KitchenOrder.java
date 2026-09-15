package com.kitchenpulse.order.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kitchen_orders")
public class KitchenOrder {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "idempotency_key", nullable = false, unique = true)
	private String idempotencyKey;

	@Column(nullable = false)
	private String status;

	@Column(nullable = false)
	private String station;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "items_json", nullable = false)
	private String itemsJson;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected KitchenOrder() {
	}

	public KitchenOrder(UUID id, UUID userId, String idempotencyKey, String status, String station, String itemsJson,
			Instant createdAt) {
		this.id = id;
		this.userId = userId;
		this.idempotencyKey = idempotencyKey;
		this.status = status;
		this.station = station;
		this.itemsJson = itemsJson;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public String getStatus() {
		return status;
	}

	public String getStation() {
		return station;
	}

	public String getItemsJson() {
		return itemsJson;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
