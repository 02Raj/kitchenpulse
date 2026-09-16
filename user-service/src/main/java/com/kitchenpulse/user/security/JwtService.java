package com.kitchenpulse.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

	private final SecretKey key;
	private final Duration ttl;

	public JwtService(
			@Value("${kitchenpulse.jwt.secret}") String secret,
			@Value("${kitchenpulse.jwt.ttl}") Duration ttl) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("kitchenpulse.jwt.secret must be at least 32 bytes");
		}
		this.key = Keys.hmacShaKeyFor(bytes);
		this.ttl = ttl;
	}

	public String issue(UUID userId, String email, List<String> roles) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userId.toString())
				.claim("email", email)
				.claim("roles", roles)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(ttl)))
				.signWith(key)
				.compact();
	}

	public Principal parse(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		List<String> roles = new ArrayList<>();
		Object raw = claims.get("roles");
		if (raw instanceof List<?> list) {
			for (Object item : list) {
				if (item != null) {
					roles.add(item.toString());
				}
			}
		}
		return new Principal(UUID.fromString(claims.getSubject()), claims.get("email", String.class), roles);
	}

	public record Principal(UUID userId, String email, List<String> roles) {
	}
}
