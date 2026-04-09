package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class SignupUpdateInfoRequest {
    private String signupId;           // 报名ID
    
    // 原始数据
    private String oldMember1Id;       // 原队员1 ID
    private String oldMember2Id;       // 原队员2 ID
    
    // 新数据
    private String newTeamName;        // 新队伍名称
    private String newTeamNameEn;      // 新队伍英文名称
    private String newLeaderId;        // 新队长ID
    private String newPrizeCoachId;    // 新证书教练ID
    private String newMember1Id;       // 新队员1 ID
    private String newMember2Id;       // 新队员2 ID
    private Integer newOccupationQuotaType; // 新占用名额类型
    private Boolean isFemaleTeam;      // 是否为女队
    private String institution;        // 单位名称
    private String taxpayerCode;       // 纳税人识别号
    private String invoiceAddress;     // 发票地址
    private String invoicePhone;       // 发票电话
    private String bankName;           // 开户银行
    private String bankCardCode;       // 银行账号
} 