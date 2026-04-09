package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class UserGetAllStudentResponse {
    String id;
    String firstName;
    String lastName;
    String firstNameEn;
    String lastNameEn;
    String phone;
    String email;
    Boolean sex;
    String degree;
    String college;
    String studentNumber;
    String enrollmentYear;
    String coachId;
}
