package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetCompetitionSignupRequest {
    int pageNow;
    int pageSize;
    int status;
    String CompetitionName;
    String TeamName;
    List<String> TeamMembersName;
    String CoachName;
    private String prizeCoachName;
    int OccupationQuotaType;
    String Season;
}
