package com.kitchenpulse.inventory.item;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("menu_items")
public class MenuItem {

	@Id
	private String id;

	@Indexed(unique = true)
	private String sku;

	private String name;
	private String station;
	private int stock;

	protected MenuItem() {
	}

	public MenuItem(String sku, String name, String station, int stock) {
		this.sku = sku;
		this.name = name;
		this.station = station;
		this.stock = stock;
	}

	public String getSku() {
		return sku;
	}

	public String getName() {
		return name;
	}

	public String getStation() {
		return station;
	}

	public int getStock() {
		return stock;
	}

	public void decrement(int quantity) {
		this.stock -= quantity;
	}
}
