package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminUpdateSystemInfoRequest {
    private Integer seasonNumber;
    private Integer schoolNumber;
    private Integer contestNumber;
    private Integer currentSeasonContestNumber;
}
