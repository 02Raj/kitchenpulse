package com.kitchenpulse.user.auth;

import com.kitchenpulse.user.common.ApiException;
import com.kitchenpulse.user.role.AppRole;
import com.kitchenpulse.user.role.AppRoleRepository;
import com.kitchenpulse.user.security.JwtService;
import com.kitchenpulse.user.user.AppUser;
import com.kitchenpulse.user.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

	private final AppUserRepository users;
	private final AppRoleRepository roles;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
			AppUserRepository users,
			AppRoleRepository roles,
			PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.users = users;
		this.roles = roles;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (users.findByEmailIgnoreCase(email).isPresent()) {
			throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
		}
		AppUser user = new AppUser(UUID.randomUUID(), email, passwordEncoder.encode(request.password()), Instant.now());
		AppRole staff = roles.findByName("STAFF")
				.orElseThrow(() -> new IllegalStateException("STAFF role missing — run Flyway migrations"));
		user.addRole(staff);
		users.save(user);
		return token(user);
	}

	public AuthResponse login(LoginRequest request) {
		AppUser user = users.findByEmailIgnoreCase(request.email().trim())
				.filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
		return token(user);
	}

	private AuthResponse token(AppUser user) {
		List<String> roleNames = user.getRoles().stream().map(AppRole::getName).sorted().toList();
		return new AuthResponse(jwtService.issue(user.getId(), user.getEmail(), roleNames), user.getId(), user.getEmail(), roleNames);
	}
}
