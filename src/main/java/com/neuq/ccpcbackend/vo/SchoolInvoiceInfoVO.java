package com.neuq.ccpcbackend.vo;

import lombok.Data;

/**
 * @author MachiskComic
 * @ClassName SchoolInvoiceInfoVO
 * @date 2025-04-28 18:46
 */
@Data
public class SchoolInvoiceInfoVO {
    private String schoolId;
    private String email;
    private String schoolName;
    private String schoolNameEn;
    private String coachName;
    private String coachNameEn;
    private String institution;
    private String taxpayerCode;
    private String mailingAddress;
    private String mailingAcceptorPhone;
    private String mailingAcceptorName;
    private String bankCardCode;
    private String bankName;
}
