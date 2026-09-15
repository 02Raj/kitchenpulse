package com.kitchenpulse.order.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KitchenOrderRepository extends JpaRepository<KitchenOrder, UUID> {

	Optional<KitchenOrder> findByIdempotencyKey(String idempotencyKey);

	List<KitchenOrder> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
