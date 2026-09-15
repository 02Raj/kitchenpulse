package com.kitchenpulse.inventory.reservation;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface StockReservationRepository extends MongoRepository<StockReservation, String> {

	boolean existsByOrderId(UUID orderId);
}
