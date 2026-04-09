package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetUserInfoByIdResponse {
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
    String wxid;
    String email;
    Integer status;

    String address;
    String addressee;
    String addressPhone;

    String coach;
    String school;

    List<String> roleIds;
}
