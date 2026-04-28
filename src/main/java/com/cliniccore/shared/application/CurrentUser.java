package com.cliniccore.shared.application;

import com.cliniccore.identity.domain.Role;
import java.util.UUID;

public record CurrentUser(UUID userId, UUID clinicId, String email, Role role) {
}
