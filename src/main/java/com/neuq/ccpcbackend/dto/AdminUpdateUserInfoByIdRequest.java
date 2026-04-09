package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminUpdateUserInfoByIdRequest {
    String id;
    String phone;
    String password;
    String schoolId;
    String coachId;
    String firstName;
    String lastName;
    String firstNameEn;
    String lastNameEn;
    String clothSize;
    Long birthday;
    String studentNumber;
    String enrollmentYear;
    String college;
    String degree;
    Boolean sex;
    String email;
    Integer status;

    String address;
    String addressee;
    String addressPhone;

    List<String> roleIds;
}
