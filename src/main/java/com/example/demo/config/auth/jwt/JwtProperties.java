package com.example.demo.config.auth.jwt;


public class JwtProperties {
    public static final int EXPIRATION_TIME = 600000; // 10분
    public static final String COOKIE_NAME = "JWT-AUTHENTICATION";
    public static final int REFRESH_TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String REFRESH_COOKIE_NAME = "REFRESH-TOKEN";
}
