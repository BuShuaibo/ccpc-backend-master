package com.neuq.ccpcbackend.service;

import com.neuq.ccpcbackend.dto.UserGetAnnouncementRequest;
import com.neuq.ccpcbackend.utils.response.Response;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
public interface IndexService {
    Response getTopCompetition();
    Response getTopAnnouncement();
    Response getAnnouncementInfo(String id);
    Response getHomeSystemInfo();
    Response getAllSponsor();

    Response getAllAnnouncement(UserGetAnnouncementRequest userGetAnnouncementRequest);

    /**
     * Get announcement content by id
     * @param id announcement id
     * @return Response containing the announcement content
     */
    Response getAnnouncementContent(String id);
}
