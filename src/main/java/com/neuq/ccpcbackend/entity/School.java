package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("school")
public class School {
    String id;
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
    String schoolAdminId;
}
