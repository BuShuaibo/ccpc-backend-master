package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class CompetitionGetInfoResponse {

    // 比赛id
    private String id;

    // 比赛名字
    private String name;

    // 比赛类型
    private String type;

    // 图片链接
    private String pictureLink;

    // 承办学校名字
    private String school;

    // 比赛开始时间 (Unix时间戳)
    private Long startTime;

    // 比赛结束时间 (Unix时间戳)
    private Long endTime;

    // 报名开始时间 (Unix时间戳)
    private Long registrationStartTime;

    // 报名结束时间 (Unix时间戳)
    private Long registrationEndTime;


    // 比赛管理员名字
    private String competitionAdminFirstName;
    private String competitionAdminLastName;

    // 赛季
    private String season;

}
