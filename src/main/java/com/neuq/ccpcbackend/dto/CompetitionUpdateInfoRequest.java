package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class CompetitionUpdateInfoRequest {
    private String id;
    public String name;

    public String type;

    public String description;

    public Long startTime;

    public Long endTime;

    public Long registrationStartTime;

    public Long registrationEndTime;

    public Long paymentDeadline;

    public Long teamInfoModifyDeadline;

    public String season;
}
