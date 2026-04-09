package com.neuq.ccpcbackend.dto;

import com.neuq.ccpcbackend.entity.CompetitionGroupCategory;
import lombok.Data;

import java.util.List;

@Data
public class AdminUpdateCompetitionInfoByIdRequest {
    public String id;
    public String name;
    public String type;

    public String schoolId;

    public String description;
    public Long startTime;
    public Long endTime;
    public Long registrationStartTime;
    public Long registrationEndTime;
    public Long paymentDeadline;
    public Long teamInfoModifyDeadline;

    public String competitionAdminId;

    public String invitationLink;
    public String scheduleLink;
    public String registeredTeamsLink;
    public String paidTeamsLink;
    public String travelAccommodationLink;
    public String awardsLink;

    public String season;

    public List<CompetitionGroupCategory> competitionGroupCategories;
}
