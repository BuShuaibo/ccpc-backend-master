package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminGetAllSchoolResponse {
    String id;
    String name;
    String nameEn;
    String mailingAddress;
    String schoolBadgeUrl;
}
