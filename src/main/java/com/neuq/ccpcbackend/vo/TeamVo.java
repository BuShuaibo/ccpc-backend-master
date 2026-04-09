package com.neuq.ccpcbackend.vo;

import com.neuq.ccpcbackend.entity.Team;
import com.neuq.ccpcbackend.entity.User;
import lombok.Data;

import java.util.List;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Data
public class TeamVo {
    String id;
    String name;
    String nameEn;
    String schoolName;
    String coachFirstName;
    String coachLastName;
    String coachName;
    String season;
    String prizeCoachName;
    String prizeCoachFirstName;
    String prizeCoachLastName;
    String prizeCoachNameEn;
    String prizeCoachFirstNameEn;
    String prizeCoachLastNameEn;
    Boolean isFemaleTeam;
    //队伍的抽象信息
    Team team;
    List<User> members;
}
