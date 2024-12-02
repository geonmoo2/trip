package com.example.demo.config.auth.loginHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorMessage = "아이디와 비밀번호가 일치하지 않습니다.";
        // URL 인코딩
        String encodedMessage = java.net.URLEncoder.encode(errorMessage, "UTF-8");
        response.sendRedirect("/user/login?error=true&message=" + encodedMessage);
    }
}
