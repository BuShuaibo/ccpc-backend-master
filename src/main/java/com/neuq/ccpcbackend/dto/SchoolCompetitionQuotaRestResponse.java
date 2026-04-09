package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class SchoolCompetitionQuotaRestResponse {
    private Long quotaSimpleRest;     // 普通名额剩余数
    private Long quotaGirlRest;       // 女队名额剩余数
    private Long quotaAdditionRest;   // 外卡名额剩余数
    private Long quotaUnofficialRest; // 打星名额剩余数
}
