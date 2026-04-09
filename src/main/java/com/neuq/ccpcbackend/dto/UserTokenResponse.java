package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserTokenResponse {
    String accessToken;
    String refreshToken;
    List<String> roles;
    UserGetBaseInfoResponse userInfo;
}
