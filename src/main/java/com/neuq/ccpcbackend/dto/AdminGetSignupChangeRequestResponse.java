package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetSignupChangeRequestResponse {
    private String id;                          // 主键ID
    private String signupId;                    // 关联报名记录ID
    private String competitionName;

    private String leaderNameBefore;              // 队长名字（修改前）
    private String leaderNameAfter;               // 队长名字（修改后）

    private List<String> teamMemberBefore;        // 队员名字（修改前）
    private List<String> teamMemberAfter;         // 队员名字（修改后）

    private String prizeCoachNameBefore;          // 证书教练名称（修改前）
    private String prizeCoachNameAfter;             // 证书教练名称（修改后）


    private String teamNameBefore;                  // 队伍名称（修改前）
    private String teamNameAfter;                   // 队伍名称（修改后）

    private String teamNameEnBefore;                // 英文队伍名（修改前）
    private String teamNameEnAfter;                 // 英文队伍名（修改后）

    private Boolean isFemaleTeamBefore;         // 是否女队（修改前）
    private Boolean isFemaleTeamAfter;          // 是否女队（修改后）

    private Integer occupationQuotaTypeBefore;  // 占用名额类型（修改前）
    private Integer occupationQuotaTypeAfter;   // 占用名额类型（修改后）

    private String institutionBefore;
    private String institutionAfter;

    private String taxpayerCodeBefore;
    private String taxpayerCodeAfter;

    private String invoiceAddressBefore;
    private String invoiceAddressAfter;

    private String invoicePhoneBefore;
    private String invoicePhoneAfter;

    private String bankNameBefore;
    private String bankNameAfter;

    private String bankCardCodeBefore;
    private String bankCardCodeAfter;

    private Integer requestStatus;              // 请求状态：1=请求中，2=通过，3=不通过

    private String reviewRemark;                // 审核备注

    private Long createdAt;                     // 创建时间
    private Long updatedAt;                     // 修改时间
}
