package com.kitchenpulse.order.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
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

	public String issue(UUID userId, String email) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userId.toString())
				.claim("email", email)
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
		return new Principal(UUID.fromString(claims.getSubject()), claims.get("email", String.class));
	}

	public record Principal(UUID userId, String email) {
	}
}
