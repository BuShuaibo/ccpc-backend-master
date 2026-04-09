package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class UserGetBaseInfoResponse {
    String id;
    String phone;
    String email;
    String firstName;
    String lastName;
    String firstNameEn;
    String lastNameEn;
    Boolean sex;
    String clothSize;
    String studentNumber;
    String enrollmentYear;
    String degree;
    String college;
    String schoolId;
    String school;
}
