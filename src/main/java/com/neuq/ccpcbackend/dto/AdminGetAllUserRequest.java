package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetAllUserRequest {
    int pageNow;
    int pageSize;
    String sortType;
    String keywordName;
    String keywordNameEn;
    String keywordSchool;
    String keywordPhone;
    String keywordEmail;
    List<String> keywordRoleIds;
}
