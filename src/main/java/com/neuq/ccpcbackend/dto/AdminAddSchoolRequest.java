package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminAddSchoolRequest {
    String name;
    String nameEn;
    String mailingAddress;
    String institution;
    String taxpayerCode;
    String invoiceAddress;
    String invoicePhone;
    String bankName;
    String bankCardCode;
    String schoolBadgeUrl;
}
