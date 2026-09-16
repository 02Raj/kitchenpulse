package com.kitchenpulse.user.profile;

import java.util.List;
import java.util.UUID;

public record UserProfileResponse(UUID id, String email, List<String> roles) {
}
