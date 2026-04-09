package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetAllUserResponse {
    String id;
    String firstName;
    String lastName;
    String firstNameEn;
    String lastNameEn;
    String phone;
    String email;
    Boolean sex;
    String school;
    List<String> roleIds;
}
