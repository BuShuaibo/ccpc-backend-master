package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class TeamAddCoachTeamRequest {
    private Boolean isFemaleTeam;
    /**
     * 队长id
     */
    private String leaderId;
    /**
     * 队员id(包括队长
     */
    private String membersIds;
    private String name;
    private String nameEn;
    private String prizeCoachId;
    /**
     * 队伍剩余名额
     */
    private double quotaRest;
    private String season;
    /**
     * 队伍类型id
     */
    private String teamTypeId;
}
