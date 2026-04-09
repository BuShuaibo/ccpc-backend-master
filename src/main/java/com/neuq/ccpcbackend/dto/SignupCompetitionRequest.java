package com.neuq.ccpcbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignupCompetitionRequest {
    @NotBlank(message = "队伍ID不能为空")
    private String teamId;               // 原始队伍ID
    
    @NotBlank(message = "学校ID不能为空")
    private String schoolId;             // 学校ID
    
    private String coachId;              // 教练ID
    
    @NotBlank(message = "队长ID不能为空")
    private String leaderId;             // 队长ID

    private String member1Id;           // 队员1 ID

    private String member2Id;           // 队员2 ID
    
    private String season;               // 所属赛年
    
    private String prizeCoachId;         // 证书教练ID
    
    @NotNull(message = "队伍类型不能为空")
    private Boolean isFemaleTeam;        // 是否是女队
    
    @NotNull(message = "占用名额类型不能为空")
    private Integer occupationQuotaType; // 占用名额类型
    
    private String teamName;             // 队伍名字
    
    private String teamNameEn;           // 队伍英文名字
    
    @NotBlank(message = "单位名称不能为空")
    private String institution;
    
    @NotBlank(message = "纳税人识别号不能为空")
    private String taxpayerCode;
    
    @NotBlank(message = "发票地址不能为空")
    private String invoiceAddress;
    
    @NotBlank(message = "发票电话不能为空")
    private String invoicePhone;
    
    @NotBlank(message = "银行名称不能为空")
    private String bankName;
    
    @NotBlank(message = "银行账号不能为空")
    private String bankCardCode;
    
    private String paymentReceiptUrl;
}
