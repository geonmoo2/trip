package com.example.demo.dto;


import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private String userName;
    private String password;
    private String realname;
    private LocalDate birth;
    private String phone;
    private String addr;
    private String email;
    private char gender;

    public void setBirth(String birth){
        if (birth != null && !birth.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            this.birth = LocalDate.parse(birth, formatter);
        }
    }
}
