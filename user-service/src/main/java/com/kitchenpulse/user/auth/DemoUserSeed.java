package com.kitchenpulse.user.auth;

import com.kitchenpulse.user.role.AppRole;
import com.kitchenpulse.user.role.AppRoleRepository;
import com.kitchenpulse.user.user.AppUser;
import com.kitchenpulse.user.user.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class DemoUserSeed implements CommandLineRunner {

	private final AppUserRepository users;
	private final AppRoleRepository roles;
	private final PasswordEncoder passwordEncoder;

	public DemoUserSeed(AppUserRepository users, AppRoleRepository roles, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.roles = roles;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		AppUser chef = users.findByEmailIgnoreCase("chef@kitchenpulse.dev").orElseGet(() -> {
			AppUser created = new AppUser(
					UUID.fromString("11111111-1111-1111-1111-111111111111"),
					"chef@kitchenpulse.dev",
					passwordEncoder.encode("chef12345"),
					Instant.now());
			return users.save(created);
		});
		roles.findByName("STAFF").ifPresent(chef::addRole);
		roles.findByName("KITCHEN").ifPresent(chef::addRole);
		users.save(chef);
	}
}
