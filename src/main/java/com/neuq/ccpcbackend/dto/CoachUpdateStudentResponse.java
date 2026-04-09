package com.neuq.ccpcbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CoachUpdateStudentResponse {
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
    String address;
    String addressee;
    String addressPhone;
}
