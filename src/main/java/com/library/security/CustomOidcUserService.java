package com.library.security;

import com.library.module.user.entity.User;
import com.library.module.user.enums.AuthProvider;
import com.library.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = resolveProvider(registrationId);

        String providerId = oidcUser.getSubject();
        String email = extractEmail(oidcUser);
        String name = extractName(oidcUser);

        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "email_not_found",
                    "Không lấy được email từ tài khoản " + provider.name(),
                    null
            ));
        }

        email = email.trim().toLowerCase();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setProfileImage("default.jpg");
            user.setRole("ROLE_USER");
            user.setIsEnable(true);
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setAuthProvider(provider);
            user.setProviderId(providerId);
            user.setEmailVerified(true);
        } else {
            if (Boolean.FALSE.equals(user.getIsEnable())) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        "account_disabled",
                        "Tài khoản của bạn đã bị vô hiệu hóa.",
                        null
                ));
            }

            if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        "account_locked",
                        "Tài khoản của bạn đang bị khóa.",
                        null
                ));
            }

            AuthProvider currentProvider = user.getAuthProvider();

            if (currentProvider != null
                    && currentProvider != AuthProvider.LOCAL
                    && currentProvider != provider) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        "provider_mismatch",
                        "Email này đã được liên kết với phương thức đăng nhập khác.",
                        null
                ));
            }

            if (currentProvider == null || currentProvider == AuthProvider.LOCAL) {
                user.setAuthProvider(provider);
                user.setProviderId(providerId);
            }

            user.setEmailVerified(true);

            if (!StringUtils.hasText(user.getName())) {
                user.setName(name);
            }

            if (user.getIsEnable() == null) {
                user.setIsEnable(true);
            }

            if (user.getAccountNonLocked() == null) {
                user.setAccountNonLocked(true);
            }

            if (user.getFailedAttempt() == null) {
                user.setFailedAttempt(0);
            }
        }

        userRepository.save(user);

        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(user.getRole()));

        if (oidcUser.getUserInfo() != null) {
            return new DefaultOidcUser(
                    authorities,
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo(),
                    "email"
            );
        }

        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                "email"
        );
    }

    private AuthProvider resolveProvider(String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return AuthProvider.GOOGLE;
        }

        throw new OAuth2AuthenticationException(new OAuth2Error(
                "unsupported_provider",
                "Provider không được hỗ trợ: " + registrationId,
                null
        ));
    }

    private String extractEmail(OidcUser oidcUser) {
        if (StringUtils.hasText(oidcUser.getEmail())) {
            return oidcUser.getEmail();
        }

        Object preferredUsername = oidcUser.getClaims().get("preferred_username");
        if (preferredUsername instanceof String value && value.contains("@")) {
            return value;
        }

        Object upn = oidcUser.getClaims().get("upn");
        if (upn instanceof String value && value.contains("@")) {
            return value;
        }

        return null;
    }

    private String extractName(OidcUser oidcUser) {
        if (StringUtils.hasText(oidcUser.getFullName())) {
            return oidcUser.getFullName();
        }

        if (StringUtils.hasText(oidcUser.getGivenName())) {
            return oidcUser.getGivenName();
        }

        String email = extractEmail(oidcUser);
        return StringUtils.hasText(email) ? email : "Người dùng";
    }
}
