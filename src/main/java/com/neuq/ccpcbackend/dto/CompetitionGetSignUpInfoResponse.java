package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

/**
 * @author MachiskComic
 * @ClassName TeamGetSignUpInfoResponse
 * @date 2025-04-24 17:20
 */
@Data
public class CompetitionGetSignUpInfoResponse {

    // 比赛id
    private String id;

    // 比赛名字
    private String name;

    // 比赛类型
    private String type;

    // 承办学校名字
    private String schoolName;

    // 比赛详情
    private String description;

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

    // 邀请函下载链接
    public String invitationLink;

    // 日程表下载链接
    public String scheduleLink;

    // 已报名队伍信息链接
    public String registeredTeamsLink;

    // 已缴费队伍信息链接
    public String paidTeamsLink;

    // 交通住宿等信息链接
    public String travelAccommodationLink;

    // 获奖信息链接
    public String awardsLink;
}
