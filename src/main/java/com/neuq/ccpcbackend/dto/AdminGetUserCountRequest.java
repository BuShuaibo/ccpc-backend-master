package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetUserCountRequest {
    String keywordName;
    String keywordNameEn;
    String keywordSchool;
    String keywordPhone;
    String keywordEmail;
    List<String> keywordRoleIds;
}
