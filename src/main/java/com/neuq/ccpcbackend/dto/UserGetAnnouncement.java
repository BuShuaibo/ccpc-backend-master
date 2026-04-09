package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class UserGetAnnouncement {
    private String id;
    private String cover_picture_url;//封面图片
    private String authorName;//作者
    private Long updateTime;//时间
    private String title;//标题
    private String summary;//摘要
    private long viewCount;//浏览次数
}