package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeamQueryResult {
    String id;
    String name;
    String nameEn;
    String schoolId;
    String coachId;
    String season;
    String prizeCoachId;
    String leaderId;
    Boolean isFemaleTeam;
    String teamType;
    List<TeamQueryResultMember> members;
    TeamQueryResultMember prizeCoach;
}
