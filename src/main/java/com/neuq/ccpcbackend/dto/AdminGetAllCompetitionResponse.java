package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminGetAllCompetitionResponse {
    public String id;
    public String name;
    public String type;

    public String schoolId;
    public String schoolName;

    public String description;
    public Long startTime;
    public Long endTime;
    public Long registrationStartTime;
    public Long registrationEndTime;
    public Long paymentDeadline;
    public Long teamInfoModifyDeadline;

    public String competitionAdminId;
    public String competitionAdminName;

    public String season;
}
