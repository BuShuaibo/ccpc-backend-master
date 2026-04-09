package com.neuq.ccpcbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateAddressInfoRequest {
    @NotBlank(message = "邮寄地址不能为空")
    String address;

    @NotBlank(message = "收件人不能为空")
    String addressee;

    @NotBlank(message = "收件号码不能为空")
    String phone;
}
