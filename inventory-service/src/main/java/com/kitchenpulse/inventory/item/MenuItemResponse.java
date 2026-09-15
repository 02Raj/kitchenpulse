package com.kitchenpulse.inventory.item;

public record MenuItemResponse(String sku, String name, String station, int stock) {

	public static MenuItemResponse from(MenuItem item) {
		return new MenuItemResponse(item.getSku(), item.getName(), item.getStation(), item.getStock());
	}
}
