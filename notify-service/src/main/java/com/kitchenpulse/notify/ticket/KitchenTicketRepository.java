package com.kitchenpulse.notify.ticket;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface KitchenTicketRepository extends MongoRepository<KitchenTicket, String> {

	Optional<KitchenTicket> findByOrderId(String orderId);

	List<KitchenTicket> findAllByOrderByCreatedAtDesc();
}
