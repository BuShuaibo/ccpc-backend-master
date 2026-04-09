package com.neuq.ccpcbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuq.ccpcbackend.dto.AdminGetAnnouncementInfoResponse;
import com.neuq.ccpcbackend.entity.Announcement;
import com.neuq.ccpcbackend.vo.AdminGetAllAnnouncementVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    long getAnnouncementsCount(Map<String, Object> params);
    List<AdminGetAllAnnouncementVo> getAllAnnouncement(Map<String, Object> params);
    AdminGetAnnouncementInfoResponse getAnnouncementInfo(String announcementId);
}
