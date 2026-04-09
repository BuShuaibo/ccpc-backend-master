package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("signup")
public class Signup {
    private String id;                   // 主键ID
    private String competitionId;        // 比赛ID
    private String teamId;               // 原始队伍ID
    private String schoolId;             // 学校ID
    private String coachId;              // 教练ID
    private String prizeCoachId;         // 证书教练ID
    private String leaderId;             // 队长ID
    private String season;               // 所属赛年
    private String teamName;                 // 队伍名称
    private String teamNameEn;               // 队伍英文名
    private Boolean isFemaleTeam;        // 是否是女队
    private Integer occupationQuotaType; // 占用名额类型
    private Integer processStatus;       // 当前流程状态
    private String institution;
    private String taxpayerCode;
    private String invoiceAddress;
    private String invoicePhone;
    private String bankName;
    private String bankCardCode;
    private String paymentReceiptUrl;
    private Long createdAt;              // 创建时间
    private Long updatedAt;              // 修改时间
    private String competitionGroupCategoryId;
}
