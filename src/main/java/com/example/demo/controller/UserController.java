package com.example.demo.controller;

import com.example.demo.Entity.User;
import com.example.demo.Service.UserService;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "message", required = false) String message,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", message != null ? message : "아이디와 비밀번호가 잘못되었습니다.");
        }
        return "login"; // login.html
    }

    // 로그인 처리
    @PostMapping("/login-proc")
    public String login(@RequestParam("userName") String username,
                        @RequestParam("password") String password,
                        HttpServletResponse response, Model model) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT 토큰 생성
            TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

            // JWT 토큰을 응답 헤더에 추가
            response.setHeader("Authorization", "Bearer " + tokenInfo.getAccessToken());

            // 로그인 성공 시 리다이렉트
            return "redirect:/content/region"; // 성공 후 이동할 경로

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", username, e);

            // 로그인 실패 시 에러 메시지와 함께 로그인 페이지로 리다이렉트
            return "redirect:/user/login?error=true&message=아이디와 비밀번호가 일치하지 않습니다.";
        }
    }
    @GetMapping("/find_username")
    public String showFindUsernamePage() {
        return "find_username";
    }

    // 아이디 찾기 요청을 처리하는 메서드
    @PostMapping("/find_username")
    public String findUsername(@RequestParam("realname") String realname,
                               @RequestParam("birth") String birth,
                               @RequestParam("email") String email,
                               Model model) {
        try {
            LocalDate birthDate = LocalDate.parse(birth); // 입력된 생년월일을 LocalDate로 변환
            String username = userService.findUsername(realname, birthDate, email);

            if (username != null) {
                model.addAttribute("message", "아이디는   " + username + "  입니다.");
            } else {
                model.addAttribute("errorMessage", "회원정보가 일치하지 않습니다. ");
            }
        } catch (DateTimeParseException e) {
            model.addAttribute("errorMessage", "생년월일 입력방식이 잘못되었습니다. YYYY-MM-DD.");
        }
        return "find_username_result";
    }

    // 프로필 페이지
    @GetMapping("/profile")
    public String showUserProfile(Authentication authentication, Model model) {
        String userName = authentication.getName();
        User user = userService.getUserInfo(userName);
        model.addAttribute("user", user);
        return "profile";
    }

    // 유저 정보 조회 API
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal User user) {
        if (user != null) {
            return ResponseEntity.ok(Collections.singletonMap("username", user.getUserName()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
    }
}