package com.example.demo.config.auth.provider;

import java.util.Map;

public interface OAuth2UserInfo {

    // 스코프 범위 확장하기
    // 핸드폰번호, 주소, 성별, 생년월일 등등. 권한부여 + 해야함

    String getName();
    String getEmail();
    String getProvider();
    String getProviderId();
    Map<String,Object> getAttributes();
}
