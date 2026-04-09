package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Data
public class TeamUpdateTeamRequest {
    String id;
    String name;
    String nameEn;
    String leaderId;
    String season;
    String prizeCoachId;
    Boolean isFemaleTeam;
    Long quotaRest;
    List<String> oldMembers;
    List<String> newMembers;
}
