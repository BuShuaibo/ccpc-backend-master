package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminUpdateSlideshowRequest {
    private String id;
    private String coverUrl;
    private String contentUrl;
    private Integer order;
    private Boolean show;
    private String title;
}
