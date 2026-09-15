package com.kitchenpulse.order.auth;

import java.util.UUID;

public record AuthResponse(String token, UUID userId, String email) {
}
