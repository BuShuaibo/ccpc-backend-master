package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class StudentQueryRequest {

    private Integer pageNow;
    private Integer pageSize;
    private String sortType;
    private String name;
    private String studentNumber;
    private String phone;
    private String college;
    private String enrollmentYear;
    private String sex;
}
