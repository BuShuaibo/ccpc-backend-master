package com.neuq.ccpcbackend.dto;

import com.neuq.ccpcbackend.vo.AdminGetAllAnnouncementVo;
import lombok.Data;

import java.util.List;

@Data
public class AdminGetAllAnnouncementResponse {
    long count;
    List<AdminGetAllAnnouncementVo> announcements;
}
