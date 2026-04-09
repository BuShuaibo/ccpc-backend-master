package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetAllSignupResponse {
    private String signupId;
    private String competition;          // 比赛名字 //
    private String competitionId;
    private String teamName;                 // 队伍名字 //
    private String teamNameEn;           // 队伍英文名字
    private String leaderName;
    private List<String> teamMembersName;    //队员名字
    private List<String> teamMembersNameEn; //队员英文名
    private String school;               // 学校 //
    private String coach;                // 教练 //
    private String season;               // 所属赛年
    private String prizeCoach;           // 证书教练 //
    private Integer processStatus;       // 当前流程状态
    private Integer occupationQuotaType; // 占用名额类型
    private Long createdAt;              // 创建时间
    private Long updatedAt;              // 修改时间
}
