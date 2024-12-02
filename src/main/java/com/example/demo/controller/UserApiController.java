package com.example.demo.controller;

import com.example.demo.Service.UserService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.Certification;
import com.siot.IamportRestClient.response.IamportResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserApiController {

    @Autowired
    private UserService userService;

    private IamportClient iamportClient;

    @Value("${iamport.api.key}")
    private String apiKey;

    @Value("${iamport.api.secret}")
    private String apiSecret;

    @PostConstruct
    private void initialize() {
        this.iamportClient = new IamportClient(apiKey, apiSecret);
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            boolean isDuplicate = userService.checkUsernameDuplicate(username);
            return ResponseEntity.ok().body(isDuplicate);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyIdentityWithIamport(@RequestBody Map<String, String> payload) {
        try {
            String impUid = payload.get("imp_uid");

            // V1 API로 본인인증 정보 조회
            IamportResponse<Certification> certificationResponse = iamportClient.certificationByImpUid(impUid);

            if (certificationResponse.getResponse() != null) {
                Certification certification = certificationResponse.getResponse();

                // 응답 데이터 구성
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("status", "success");
                responseData.put("name", certification.getName());
                responseData.put("phone", certification.getPhone());

                log.info("본인인증 성공: {}", responseData);

                return ResponseEntity.ok(responseData);
            }

            return ResponseEntity.badRequest()
                    .body(Map.of("status", "fail", "message", "Invalid certification"));

        } catch (Exception e) {
            log.error("본인인증 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error",
                            "message", "본인인증 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
