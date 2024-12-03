package com.example.demo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/custom-error")
public class CustomErrorController {

    @GetMapping
    @ResponseBody
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String message = "Custom error page. Please contact support if the problem persists.";

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            return new ResponseEntity<>(message + " (Error Code: " + statusCode + ")", HttpStatus.valueOf(statusCode));
        }

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
