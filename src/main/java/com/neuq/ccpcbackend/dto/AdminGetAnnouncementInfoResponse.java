package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminGetAnnouncementInfoResponse {
    public String id;
    public String title;
    public String author;
    public String summary;
    public String coverPictureUrl;
    public String content;
    public Long createAt;
    public Long updateAt;
    public Integer status;
    public Long viewCount;
    public String season;
}
