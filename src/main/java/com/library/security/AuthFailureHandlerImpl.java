package com.library.security;

import com.library.module.user.entity.User;
import com.library.module.user.repository.UserRepository;
import com.library.module.user.service.UserService;
import com.library.util.AppConstant;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("email");
        String message = "Sai thông tin đăng nhập";

        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmail(email.trim());

            if (user != null) {
                if (!Boolean.TRUE.equals(user.getIsEnable())) {
                    message = "Tài khoản chưa được kích hoạt";
                } else if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
                    if (userService.unlockAccountTimeExpired(user)) {
                        message = "Tài khoản đã được mở khóa, hãy đăng nhập lại";
                    } else {
                        message = "Tài khoản bị khóa, thử lại sau";
                    }
                } else {
                    int failedAttempt = user.getFailedAttempt() == null ? 0 : user.getFailedAttempt();
                    int nextFailedAttempt = failedAttempt + 1;

                    userService.increaseFailedAttempt(user);

                    if (nextFailedAttempt >= AppConstant.ATTEMPT_TIME) {
                        userService.userAccountLock(user);
                        message = "Tài khoản bị khóa sau " + AppConstant.ATTEMPT_TIME + " lần sai";
                    } else {
                        int remainingAttempt = AppConstant.ATTEMPT_TIME - nextFailedAttempt;
                        message = "Sai thông tin đăng nhập. Bạn còn " + remainingAttempt + " lần thử";
                    }
                }
            }
        }

        setDefaultFailureUrl("/signin?error");
        super.onAuthenticationFailure(
                request,
                response,
                new BadCredentialsException(message)
        );
    }
}