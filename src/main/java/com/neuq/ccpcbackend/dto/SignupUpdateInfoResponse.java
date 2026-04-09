package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class SignupUpdateInfoResponse {
    private Boolean directUpdate;      // 是否直接更新 (true:直接更新, false:提交申请)
    private String requestId;          // 修改申请ID (仅当directUpdate=false时有值)
    private String message;            // 操作结果描述
} 