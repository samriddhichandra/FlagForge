package com.flagforge.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Referenced in controllers as @PreAuthorize("@rbac.hasRole(#projectId, 'EDITOR')").
 *
 * Deliberately checks the membership table on every call rather than trusting a role
 * claim baked into the JWT — so a permission downgrade (e.g. Admin demotes a user to
 * Viewer) takes effect on the very next request, instead of waiting up to 15 minutes
 * for the access token to expire. This is a small extra DB read traded for correctness;
 * worth calling out explicitly in an interview as a deliberate trade-off.
 */
@Component("rbac")
public class RbacEvaluator {

    // In the full implementation this is backed by MembershipRepository.
    // Scaffolded here to show the intended shape.
    public boolean hasRole(UUID projectId, String requiredRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        // TODO: look up membership role for (currentUserId, project.organizationId)
        // and compare against a role hierarchy: OWNER > ADMIN > EDITOR > VIEWER
        return true; // placeholder — replaced by real membership lookup
    }
}
