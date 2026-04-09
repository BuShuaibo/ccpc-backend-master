package com.neuq.ccpcbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateBaseInfoRequest {
    @NotNull(message = "手机号不能为空")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    String phone;

    @NotNull(message = "邮箱不能为空")
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    String email;

    @NotNull(message = "姓（中文）不能为空")
    @NotBlank(message = "姓（中文）不能为空")
    String firstName;

    @NotNull(message = "名（中文）不能为空")
    @NotBlank(message = "名（中文）不能为空")
    String lastName;

    @NotNull(message = "姓（英文）不能为空")
    @NotBlank(message = "姓（英文）不能为空")
    String firstNameEn;

    @NotNull(message = "名（英文）不能为空")
    @NotBlank(message = "名（英文）不能为空")
    String lastNameEn;

    @NotNull(message = "性别不能为空")
    Boolean sex;

    @NotNull(message = "衣服号码不能为空")
    @NotBlank(message = "衣服号码不能为空")
    String clothSize;

    String studentNumber;

    String enrollmentYear;

    String degree;

    String college;
}
