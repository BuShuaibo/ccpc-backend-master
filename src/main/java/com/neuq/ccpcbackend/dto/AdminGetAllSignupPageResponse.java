package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;
@Data
public class AdminGetAllSignupPageResponse {
    List<AdminGetAllSignupResponse> signups;
    Long totalCount;
}
