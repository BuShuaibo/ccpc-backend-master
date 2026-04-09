package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminAddSlideshowRequest {
    private String coverUrl;
    private String contentUrl;
    private Integer order;
    private Boolean show;
    private String title;
}
