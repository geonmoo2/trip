package com.example.demo.controller;


import com.example.demo.Service.UserService;
import com.example.demo.dto.UserDTO;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/register")
public class registerController {


    private final UserService userService;
    private final UserRepository userRepository;



    @GetMapping("")  // /register 경로로 접근했을 때
    public String registerForm() {
        return "register";  // register.html 파일을 찾아 보여줍니다
    }

    @PostMapping("")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        try {
            userService.userJoin(userDTO);
            return ResponseEntity.ok().body(Map.of("message", "회원가입 성공"));
        } catch (ResponseStatusException e) {
            // ResponseStatusException에서 상태코드와 메시지를 클라이언트에 전달
            log.warn("회원가입 실패: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason()));
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("회원가입 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "회원가입 중 문제가 발생했습니다."));
        }
    }
    @GetMapping("/check-username")
    @ResponseBody
    public ResponseEntity<String> checkUsername(@RequestParam("username") String username) {
        log.info("중복 확인 요청: username={}", username);

        // 입력값 검증: username이 비어있거나 null인 경우 처리
        if (username == null || username.trim().isEmpty()) {
            log.warn("입력된 username이 비어있음");
            return ResponseEntity.badRequest().body("Invalid username input");
        }

        try {
            // 데이터베이스에서 username 존재 여부 확인
            boolean exists = userRepository.existsByUserName(username);

            // 로깅
            log.info("중복 확인 결과 (username={}): {}", username, exists);

            // 중복 여부에 따라 결과 반환
            return ResponseEntity.ok(exists ? "duplicate" : "available");
        } catch (Exception e) {
            // 예외 발생 시 로그와 함께 오류 응답 반환
            log.error("중복 확인 처리 중 오류 발생 (username={})", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<String> checkEmail(@RequestParam("email") String email) {
        log.info("이메일 중복 확인 요청: email={}", email);

        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            log.info("이메일 중복 확인 결과: 중복");
            return ResponseEntity.ok("duplicate"); // 중복된 이메일
        }
        log.info("이메일 중복 확인 결과: 사용 가능");
        return ResponseEntity.ok("available"); // 사용 가능한 이메일
    }


}
