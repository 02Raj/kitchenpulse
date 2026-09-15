package com.kitchenpulse.inventory.item;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MenuItemRepository extends MongoRepository<MenuItem, String> {

	Optional<MenuItem> findBySku(String sku);
}
