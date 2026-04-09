package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Data
public class TeamAddStudentTeamRequest {
    String name;
    String nameEn;
    String schoolId;
    String coachId;
    String prizeCoachId;
    Boolean isFemaleTeam;
    String teamTypeId;
    Long quotaRest;
}
