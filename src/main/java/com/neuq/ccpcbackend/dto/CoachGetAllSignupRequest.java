package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class CoachGetAllSignupRequest {
    int pageNow;
    int pageSize;
    private String competition;          // 比赛名字
    private String teamName;             // 队伍名字
    private String teamMember;           // 队伍成员名字
    private String prizeCoach;           // 证书教练
    private Integer processStatus;       // 当前流程状态
    private Integer occupationQuotaType; // 占用名额类型
}
