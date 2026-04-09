package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminUploadQuotationExcelResponse {
    public Integer isSuccess;
    public String message;
    public String schoolName;

    public String quotaSimple;
    public String quotaGirl;
    public String quotaAddition;
    public String quotaUnofficial;
}
