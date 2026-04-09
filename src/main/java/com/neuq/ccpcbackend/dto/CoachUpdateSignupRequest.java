package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoachUpdateSignupRequest {
    private String competitionId;        // 比赛ID
    private String teamId;               // 原始队伍ID
    private List<String> teamMembersId;  // 队员ID
    private String schoolId;             // 学校ID
    private String coachId;              // 教练ID
    private String leaderId;             // 队长ID
    private String season;               // 所属赛年
    private String prizeCoachId;         // 证书教练ID
    private Boolean isFemaleTeam;        // 是否是女队
    private Integer processStatus;       // 当前流程状态
    private Integer occupationQuotaType; // 占用名额类型
    private String teamName;             // 队伍名字
    private String teamNameEn;           // 队伍英文名字
}
