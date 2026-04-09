package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("team")
public class Team {
    @TableId
    String id;

    String name;
    String nameEn;
    String schoolId;
    String coachId;
    String leaderId;
    String season;
    String prizeCoachId;
    Boolean isFemaleTeam;
    String teamTypeId;
    Long quotaRest;
}
