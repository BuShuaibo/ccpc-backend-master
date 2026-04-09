package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class TeamQueryResultMember {
    String id;
    String firstName;
    String lastName;
    String firstNameEn;
    String lastNameEn;
    String phone;
    String email;
}
