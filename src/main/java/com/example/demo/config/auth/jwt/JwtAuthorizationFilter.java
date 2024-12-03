package com.example.demo.config.auth.jwt;


import com.example.demo.Entity.User;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthorizationFilter(
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException, IOException {
        System.out.println("[JWTAUTHORIZATIONFILTER] doFilterInternal...");

        String token = null;
        try {
            // cookie 에서 JWT token을 가져옵니다.
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(JwtProperties.COOKIE_NAME)).findFirst()
                    .map(cookie -> cookie.getValue())
                    .orElse(null);
        } catch (Exception ignored) {

        }
        if (token != null) {
            try {
                if(jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = getUsernamePasswordAuthenticationToken(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("[JWTAUTHORIZATIONFILTER] : " + authentication);
                }
            } catch (ExpiredJwtException e)     //토큰만료시 예외처리(쿠키 제거)
            {
                System.out.println("[JWTAUTHORIZATIONFILTER] : ...ExpiredJwtException ...."+e.getMessage());
                //토큰 만료시 처리(Refresh-token으로 갱신처리는 안함-쿠키에서 제거)
                handleTokenExpiration(request,response);
//                Cookie cookie = new Cookie(JwtProperties.COOKIE_NAME, null);
//                cookie.setMaxAge(0);
//                response.addCookie(cookie);

            }catch(Exception e2){

            }
        }
        chain.doFilter(request, response);
    }

    /**
     * JWT 토큰으로 User를 찾아서 UsernamePasswordAuthenticationToken를 만들어서 반환한다.
     * User가 없다면 null
     */

    private Authentication getUsernamePasswordAuthenticationToken(String token) {

        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        Optional<User> user = userRepository.findByUserName(authentication.getName()); // 유저를 유저명으로 찾습니다.
        System.out.println("JwtAuthorizationFilter.getUsernamePasswordAuthenticationToken...authenticationToken : " +authentication );
        if(user!=null)
        {
            return authentication;
        }
        return null; // 유저가 없으면 NULL
    }

    private void handleTokenExpiration(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Refresh Token을 검사하여 새로운 Access Token 발급 로직 추가
        Cookie refreshTokenCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(JwtProperties.REFRESH_COOKIE_NAME)).findFirst()
                .orElse(null);

        if (refreshTokenCookie != null) {
            String refreshToken = refreshTokenCookie.getValue();
            if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
                // Refresh Token 유효성 검증 성공 시, 새로운 Access Token 발급
                String newAccessToken = jwtTokenProvider.generateAccessTokenFromRefreshToken(refreshToken);
                // 새로운 Access Token을 Response의 Header나 Cookie 등에 설정
                response.setHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX+newAccessToken);
            } else {
                // Refresh Token 유효성 검증 실패 시, 로그아웃 처리 등 추가 작업 수행
                // 예시: Refresh Token을 삭제하여 로그아웃 처리
                Cookie cookie = new Cookie(JwtProperties.REFRESH_COOKIE_NAME, null);
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }


}
