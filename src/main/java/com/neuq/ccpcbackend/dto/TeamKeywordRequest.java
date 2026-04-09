package com.neuq.ccpcbackend.dto;

import lombok.Data;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Data
public class TeamKeywordRequest {
    String name;
    String nameEn;
    String schoolName;
    String coachName;
    String season;
    String prizeCoachName;
    Boolean isFemaleTeam;
}
