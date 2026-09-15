package com.kitchenpulse.order.auth;

import com.kitchenpulse.order.common.ApiException;
import com.kitchenpulse.order.security.JwtService;
import com.kitchenpulse.order.user.AppUser;
import com.kitchenpulse.order.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

	private final AppUserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.users = users;
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
		return new AuthResponse(jwtService.issue(user.getId(), user.getEmail()), user.getId(), user.getEmail());
	}
}
