package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateRequest {
    private String email;
    private String phoneNumber;
    private String address;
    private String realName;
    private LocalDate birthDate;
    private char gender;
}
