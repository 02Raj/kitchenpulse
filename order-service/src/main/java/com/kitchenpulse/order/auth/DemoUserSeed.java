package com.kitchenpulse.order.auth;

import com.kitchenpulse.order.user.AppUser;
import com.kitchenpulse.order.user.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class DemoUserSeed implements CommandLineRunner {

	private final AppUserRepository users;
	private final PasswordEncoder passwordEncoder;

	public DemoUserSeed(AppUserRepository users, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		users.findByEmailIgnoreCase("chef@kitchenpulse.dev").orElseGet(() -> users.save(
				new AppUser(
						UUID.fromString("11111111-1111-1111-1111-111111111111"),
						"chef@kitchenpulse.dev",
						passwordEncoder.encode("chef12345"),
						Instant.now())));
	}
}
