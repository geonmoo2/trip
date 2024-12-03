package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {
    private String grantType;    // 토큰 타입 (예: Bearer)
    private String accessToken;  // 액세스 토큰
    private String refreshToken; // 리프레시 토큰

    // 기존 생성자 추가 시, 필요한 경우 추가적인 생성자 생성
    public TokenInfo(String accessToken) {
        this.accessToken = accessToken;
    }
}
