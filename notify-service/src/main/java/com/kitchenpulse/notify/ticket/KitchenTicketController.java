package com.kitchenpulse.notify.ticket;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kitchen/tickets")
public class KitchenTicketController {

	private final KitchenTicketRepository tickets;

	public KitchenTicketController(KitchenTicketRepository tickets) {
		this.tickets = tickets;
	}

	@GetMapping
	public List<KitchenTicketResponse> list() {
		return tickets.findAllByOrderByCreatedAtDesc().stream().map(KitchenTicketResponse::from).toList();
	}
}
