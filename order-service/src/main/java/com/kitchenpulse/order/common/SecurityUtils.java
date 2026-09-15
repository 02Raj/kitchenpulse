package com.kitchenpulse.order.common;

import com.kitchenpulse.order.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static UUID currentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof JwtService.Principal principal)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
		}
		return principal.userId();
	}
}
