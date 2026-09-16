package com.kitchenpulse.user.profile;

import com.kitchenpulse.user.role.AppRole;
import com.kitchenpulse.user.security.JwtService;
import com.kitchenpulse.user.user.AppUser;
import com.kitchenpulse.user.user.AppUserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final AppUserRepository users;

	public UserController(AppUserRepository users) {
		this.users = users;
	}

	@GetMapping("/me")
	public UserProfileResponse me(@AuthenticationPrincipal JwtService.Principal principal) {
		return new UserProfileResponse(principal.userId(), principal.email(), principal.roles());
	}

	@GetMapping
	public List<UserProfileResponse> list() {
		return users.findAll().stream()
				.map(this::toProfile)
				.sorted(Comparator.comparing(UserProfileResponse::email))
				.toList();
	}

	private UserProfileResponse toProfile(AppUser user) {
		List<String> roleNames = user.getRoles().stream().map(AppRole::getName).sorted().toList();
		return new UserProfileResponse(user.getId(), user.getEmail(), roleNames);
	}
}
