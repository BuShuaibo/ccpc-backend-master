package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class UserGetAnnouncementRequest {
    private Integer pageNow;//当前页码
    private Integer pageSize;//页面大小
    private String title;//标题
    private String authorName;//作者名
    private String season;//赛季
}