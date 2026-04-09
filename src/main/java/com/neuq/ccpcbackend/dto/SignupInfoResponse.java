package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class SignupInfoResponse {
    // 比赛名称
    private String competitionName;
    
    // 队伍信息
    private String teamName;
    private String teamNameEn;
    
    // 队长信息
    private String leaderId;
    private String leaderName;
    private String leaderNameEn;
    
    // 队员1信息
    private String member1Id;
    private String member1Name;
    private String member1NameEn;
    
    // 队员2信息
    private String member2Id;
    private String member2Name;
    private String member2NameEn;
    
    // 证书教练信息
    private String prizeCoachId;
    private String prizeCoachName;
    private String prizeCoachNameEn;
    
    // 其他字段
    private Integer occupationQuotaType;
    private String occupationQuotaTypeName;
    private Boolean isFemaleTeam;
    
    // 发票信息
    private String institution;
    private String taxpayerCode;
    private String invoiceAddress;
    private String invoicePhone;
    private String bankName;
    private String bankCardCode;
} 