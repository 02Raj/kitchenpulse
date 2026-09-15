package com.kitchenpulse.inventory.item;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/items")
public class MenuItemController {

	private final MenuItemRepository items;

	public MenuItemController(MenuItemRepository items) {
		this.items = items;
	}

	@GetMapping
	public List<MenuItemResponse> list() {
		return items.findAll().stream().map(MenuItemResponse::from).toList();
	}
}
