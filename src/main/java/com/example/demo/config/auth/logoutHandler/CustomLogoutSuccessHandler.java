package com.example.demo.config.auth.logoutHandler;


import com.example.demo.config.PrincipalDetails;
import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.util.Arrays;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    //@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String KAKAO_CLIENT_ID="5402f7706d907ecbd9f168f8102b1fae";
    //@Value("${spring.security.oauth2.client.kakao.logout.redirect.uri}")
    private String KAKAO_LOGOUT_REDIRECT_URI="http://localhost:8080/user/login";

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("CustomLogoutSuccessHandler's onLogoutSuccess()");
        System.out.println("KAKAO_CLIENT_ID : " + KAKAO_CLIENT_ID);
        System.out.println("KAKAO_LOGOUT_REDIRECT_URI : " + KAKAO_LOGOUT_REDIRECT_URI);


        //----------------------------
        //JWT
        //----------------------------
        //Cookie -> JWT Token 가져오기
        String token = Arrays.stream(request.getCookies())
                .filter(cookie->cookie.getName().equals(JwtProperties.COOKIE_NAME)).findFirst()
                .map(cookie -> cookie.getValue())
                .orElse(null);
        if(token!=null)
            authentication = jwtTokenProvider.getAuthentication(token);


        PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal();
        String provider = principalDetails.getOauserDto().getProvider();




        //Kakao Server Disconn...
        if(provider!=null &&"kakao".equals(provider)) {
            response.sendRedirect("https://kauth.kakao.com/oauth/logout?client_id=" + KAKAO_CLIENT_ID + "&logout_redirect_uri=" + KAKAO_LOGOUT_REDIRECT_URI);
            return ;
        }else if(provider!=null &&"naver".equals(provider)){
            response.sendRedirect("https://nid.naver.com/nidlogin.logout?returl=https://www.naver.com/");
            return ;
        }else if(provider!=null &&"google".equals(provider)){
            response.sendRedirect("https://accounts.google.com/Logout");
            return ;
        }
        response.sendRedirect("/content/region");
    }
}
