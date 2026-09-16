package com.kitchenpulse.gateway.security;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

	private final JwtService jwtService;

	public JwtGlobalFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getPath().value();
		if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()) || isPublic(path)) {
			return chain.filter(exchange);
		}
		String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}
		try {
			JwtService.Principal principal = jwtService.parse(header.substring(7));
			if (!allowed(path, principal.roles())) {
				exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
				return exchange.getResponse().setComplete();
			}
			String rolesHeader = String.join(",", principal.roles());
			ServerWebExchange mutated = exchange.mutate()
					.request(builder -> builder
							.header("X-User-Id", principal.userId().toString())
							.header("X-User-Email", principal.email() == null ? "" : principal.email())
							.header("X-User-Roles", rolesHeader))
					.build();
			return chain.filter(mutated);
		} catch (Exception ex) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}
	}

	private static boolean isPublic(String path) {
		return path.startsWith("/api/auth") || path.startsWith("/actuator");
	}

	private static boolean allowed(String path, List<String> roles) {
		if (path.startsWith("/api/kitchen")) {
			return hasAny(roles, "STAFF", "KITCHEN", "ADMIN");
		}
		if (path.startsWith("/api/users") && !path.equals("/api/users/me")) {
			return hasAny(roles, "ADMIN");
		}
		if (path.startsWith("/api/orders") || path.startsWith("/api/inventory")) {
			return hasAny(roles, "STAFF", "ADMIN");
		}
		return true;
	}

	private static boolean hasAny(List<String> roles, String... required) {
		for (String role : roles) {
			String normalized = role.toUpperCase(Locale.ROOT);
			for (String need : required) {
				if (normalized.equals(need) || normalized.equals("ROLE_" + need)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getOrder() {
		return -100;
	}
}
