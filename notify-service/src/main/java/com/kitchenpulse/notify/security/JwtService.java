package com.kitchenpulse.notify.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class JwtService {

	private final SecretKey key;

	public JwtService(@Value("${kitchenpulse.jwt.secret}") String secret) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("kitchenpulse.jwt.secret must be at least 32 bytes");
		}
		this.key = Keys.hmacShaKeyFor(bytes);
	}

	public UUID parseUserId(String token) {
		Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		return UUID.fromString(claims.getSubject());
	}
}
