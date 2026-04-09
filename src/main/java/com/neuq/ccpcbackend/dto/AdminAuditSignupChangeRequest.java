package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminAuditSignupChangeRequest {
    //ID
    private String id;
    //需要修改的状态：1=请求中，2=通过，3=不通过
    private int requestStatus;
    //审核意见
    private String reviewRemark;
}
