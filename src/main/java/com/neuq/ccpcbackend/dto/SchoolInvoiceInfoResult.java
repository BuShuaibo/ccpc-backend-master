package com.neuq.ccpcbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author MachiskComic
 * @ClassName SchoolInvoiceInfoResponse
 * @date 2025-04-24 18:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolInvoiceInfoResult {
    private String institution;
    private String taxpayerCode;
    private String invoiceAddress;
    private String invoicePhone;
    private String bankName;
    private String bankCardCode;
    private String paymentReceiptUrl;
}
