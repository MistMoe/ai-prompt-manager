package com.example.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private Long userId;

    private String userName;

    private String nickName;
}