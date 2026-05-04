package com.javainternship.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_ADMIN::equals);
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        String principal = authentication.getName();
        try {
            return Long.parseLong(principal);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean isOwnerOrAdmin(Long resourceOwnerId) {
        if (isCurrentUserAdmin()) {
            return true;
        }
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(resourceOwnerId);
    }
}
