package com.kitchenpulse.inventory.reservation;

import com.kitchenpulse.inventory.item.MenuItem;
import com.kitchenpulse.inventory.item.MenuItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

	private final MenuItemRepository items;
	private final StockReservationRepository reservations;

	public ReservationService(MenuItemRepository items, StockReservationRepository reservations) {
		this.items = items;
		this.reservations = reservations;
	}

	public ReserveResponse reserve(ReserveRequest request) {
		if (reservations.existsByOrderId(request.orderId())) {
			return new ReserveResponse(true, "already-reserved");
		}
		List<StockReservation.Line> lines = new ArrayList<>();
		for (ReserveRequest.ReserveLine line : request.lines()) {
			MenuItem item = items.findBySku(line.sku())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Unknown sku " + line.sku()));
			if (item.getStock() < line.quantity()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for " + line.sku());
			}
			item.decrement(line.quantity());
			items.save(item);
			lines.add(new StockReservation.Line(line.sku(), line.quantity()));
		}
		reservations.save(new StockReservation(request.orderId(), lines, Instant.now()));
		return new ReserveResponse(true, null);
	}
}
