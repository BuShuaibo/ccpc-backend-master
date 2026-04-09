package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {
    String id;
    String phone;
    String password;
    String schoolId;
    String coachId;
    Long birthday;
    String studentNumber;
    String enrollmentYear;
    String college;
    String degree;
    Boolean sex;
    String wxid;
    String email;
    Integer status;
    String firstName;
    String lastName;
    String firstNameEn;
    String lastNameEn;
    String address;
    String addressee;
    String addressPhone;
    String clothSize;
}
