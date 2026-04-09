package com.neuq.ccpcbackend.entity;

import lombok.Data;

@Data
public class RankDetail {
    // 打星队伍该字段为-1
    Integer rank;
    String signupId;
    Integer solutionNumber;
    Integer penaltyTime;
    Integer schoolRank;
    String account;
}
