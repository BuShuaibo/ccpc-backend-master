package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class SignupGetAllCoachTeamResponse {
    String id;
    String teamName;
    String teamNameEn;
    String schoolId;
    String coachId;
    String season;
    String prizeCoachId;
    List<SignupGetAllCoachTeamResponseMember> members;
    SignupGetAllCoachTeamResponseMember prizeCoach;

}
