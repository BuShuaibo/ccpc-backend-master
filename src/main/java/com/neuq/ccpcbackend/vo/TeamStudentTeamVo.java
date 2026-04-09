package com.neuq.ccpcbackend.vo;

import lombok.Data;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Data
public class TeamStudentTeamVo {
    String id;
    String name;
    String nameEn;
    String school;
    String coach;
    String leader;
    String season;
    String prizeCoach;
    Boolean isFemaleTeam;
}
