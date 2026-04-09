package com.neuq.ccpcbackend.dto;

import lombok.Data;

/**
 * DTO for returning the content of an announcement
 */
@Data
public class AnnouncementContentResponse {
    private String id;
    private String cover_picture_url;
    private String authorName;
    private Long updateTime;
    private String title;
    private String summary;
    private Long viewCount;
    private String content;
}
