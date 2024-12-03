package com.example.demo.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@Entity
@AllArgsConstructor
@Getter
@Setter
@Data
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name")
    private String userName;
    private String password;
    private String realname;
    private LocalDate birth;
    private char gender;
    private String email;
    private String addr;
    private String phoneNumber;

    private String role;        // 권한 (ROLE_USER, ROLE_ADMIN 등)
    private String provider;    // OAuth2 제공자 (google, naver, kakao 등)
    private String providerId;  // OAuth2 제공자의 사용자 식별자

    @Column(name = "islocked", nullable = false, columnDefinition = "boolean default false")
    private boolean isLocked = false;

}
