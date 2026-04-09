package com.neuq.ccpcbackend.dto;

import com.neuq.ccpcbackend.entity.CompetitionGroupCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminAddCompetitionRequest {
    @NotNull(message = "比赛名不能为空")
    @NotBlank(message = "比赛名不能为空")
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

    List<CompetitionGroupCategory> competitionGroupCategories;
}
