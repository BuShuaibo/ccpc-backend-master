package com.neuq.ccpcbackend.vo;

import lombok.Data;

@Data
public class AdminGetAllAnnouncementVo {
    public String id;
    public String title;
    public String author;
    public String summary;
    public String coverPictureUrl;
    public Long createAt;
    public Long updateAt;
    public Integer status;
    public Long viewCount;
    public String season;
}
