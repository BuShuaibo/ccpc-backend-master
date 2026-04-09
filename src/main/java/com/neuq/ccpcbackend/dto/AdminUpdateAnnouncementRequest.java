package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminUpdateAnnouncementRequest {
    String id;
    String title;
    String summary;
    String coverPictureUrl;
    String content;
    Integer status;
    String season;
}
