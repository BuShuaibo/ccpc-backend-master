package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminUpdateQuotationRequest {
    private List<Quotation> quotations;
    @Data
    public static class Quotation
    {
        public String competitionId;
        public String schoolName;
        public Long quotaSimple;
        public Long quotaGirl;
        public Long quotaAddition;
        public Long quotaUnofficial;
    }

}
