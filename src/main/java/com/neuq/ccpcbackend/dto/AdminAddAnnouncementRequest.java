package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminAddAnnouncementRequest {
    public String title;
    public String summary;
    public String coverPictureUrl;
    public String content;
    public Integer status;
    public String season;
}
