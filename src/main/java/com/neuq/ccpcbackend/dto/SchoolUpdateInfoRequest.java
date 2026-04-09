package com.neuq.ccpcbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SchoolUpdateInfoRequest {
    @NotBlank(message = "学校英文名不能为空")
    String nameEn;

    @NotBlank(message = "学校通信地址不能为空")
    String mailingAddress;

    @NotBlank(message = "单位名称不能为空")
    String institution;

    @NotBlank(message = "纳税人识别号不能为空")
    String taxpayerCode;

    @NotBlank(message = "地址不能为空")
    String address;

    @NotBlank(message = "电话不能为空")
    String phone;

    @NotBlank(message = "银行名称不能为空")
    String bankName;

    @NotBlank(message = "银行账号不能为空")
    String bankCardCode;
}
