package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/test")
    public String showTestPage() {
        // "test.html"을 반환하여 브라우저에서 인증 버튼을 테스트할 수 있습니다.
        return "test";
    }
}
