package com.neuq.ccpcbackend.dto;

import lombok.Data;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Data
public class UserKeywordRequest {
    private String firstName;
    private String lastName;
    private String firstNameEn;
    private String lastNameEn;
    private String fullName;
    private String fullNameEn;
}
