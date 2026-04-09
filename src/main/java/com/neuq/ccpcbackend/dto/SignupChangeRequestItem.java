package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class SignupChangeRequestItem {
    private String id;
    private Integer requestStatus;
    private Long createdAt;
    private Long updatedAt;
    private String reviewRemark;
} 