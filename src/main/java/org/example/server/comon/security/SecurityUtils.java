package org.example.server.common.security;

import lombok.experimental.UtilityClass;
import org.example.server.common.exception.ForbiddenOperationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtils {

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ForbiddenOperationException("User is not authenticated");
        }

        return authentication.getName();
    }
}