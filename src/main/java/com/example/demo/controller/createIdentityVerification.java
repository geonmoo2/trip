package com.example.demo.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class createIdentityVerification {

    @Value("${iamport.api.secret}")
    private String apiSecret;

    @PostMapping("/identity-verification/prepare")
    public ResponseEntity<?> prepareIdentityVerification(@RequestBody Map<String, String> payload) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "PortOne " + apiSecret);

            Map<String, String> requestBody = Map.of(
                    "storeId", "store-cce8cdb4-a54c-4044-8bfa-6533e9e4c6fb",
                    "channelKey", "danal"
            );

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            log.info("PortOne API 호출 시작 - payload: {}", payload);

            String apiUrl = "https://api.portone.io/v2/identity-verifications";

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            log.info("PortOne API 응답: {}", response.getBody());

            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("API 호출 실패: {}", e.getResponseBodyAsString(), e);
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("본인인증 준비 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}