package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("competition")
public class Competition {

    // 比赛id
    public String id;

    // 比赛名字
    public String name;

    // 比赛类型
    public String type;

    // 承办学校id
    public String schoolId;

    // 比赛详情
    public String description;

    // 比赛开始时间 (Unix时间戳)
    public Long startTime;

    // 比赛结束时间 (Unix时间戳)
    public Long endTime;

    // 报名开始时间 (Unix时间戳)
    public Long registrationStartTime;

    // 报名结束时间 (Unix时间戳)
    public Long registrationEndTime;

    // 缴费截止时间 (Unix时间戳)
    public Long paymentDeadline;

    // 队伍信息修改截止时间 (Unix时间戳)
    public Long teamInfoModifyDeadline;

    // 比赛管理员ID
    public String competitionAdminId;

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

    // 创建时间
    public Long createdAt;

    // 修改时间
    public Long updatedAt;

    // 赛季
    public String season;

    // 比赛图片
    public String pictureLink;

    // 这场比赛是否需要缴费凭证流程
    public Boolean needPaymentReceipt;

    // 这场比赛是否需要现场签到流程
    public Boolean needSignIn;

    // 这场比赛是否需要上传照片
    public Boolean needUploadPicture;
}
