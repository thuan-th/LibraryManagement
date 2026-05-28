package com.library.security;

import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        resetFailedAttempt(authentication);

        var roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/");
        } else if (roles.contains("ROLE_USER")) {
            response.sendRedirect("/");
        } else {
            response.sendRedirect("/signin?error");
        }
    }

    private void resetFailedAttempt(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return;
        }

        String email = authentication.getName();

        if (authentication.getPrincipal() instanceof OidcUser oidcUser && oidcUser.getEmail() != null) {
            email = oidcUser.getEmail();
        }

        User user = userService.getUserByEmail(email);

        if (user == null) {
            return;
        }

        Integer failedAttempt = user.getFailedAttempt();

        if (failedAttempt == null || failedAttempt > 0 || user.getLockTime() != null) {
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userService.updateUser(user);
        }
    }
}
