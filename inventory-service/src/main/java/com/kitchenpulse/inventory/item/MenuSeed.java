package com.kitchenpulse.inventory.item;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MenuSeed implements CommandLineRunner {

	private final MenuItemRepository items;

	public MenuSeed(MenuItemRepository items) {
		this.items = items;
	}

	@Override
	public void run(String... args) {
		seed("BURGER", "Classic burger", "GRILL", 40);
		seed("FRIES", "Fries", "FRY", 80);
		seed("SHAKE", "Vanilla shake", "BAR", 30);
	}

	private void seed(String sku, String name, String station, int stock) {
		if (items.findBySku(sku).isEmpty()) {
			items.save(new MenuItem(sku, name, station, stock));
		}
	}
}
