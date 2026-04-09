package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserGetAnnouncementResponse {
    private List<UserGetAnnouncement> userGetAnnouncements;
    private Long totalCount;

}
