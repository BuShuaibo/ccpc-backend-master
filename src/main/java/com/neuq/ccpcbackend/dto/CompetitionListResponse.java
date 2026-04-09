package com.neuq.ccpcbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CompetitionListResponse {

    // 比赛信息列表
    private List<CompetitionGetInfoResponse> competitions;

    // 比赛总条数（用于分页）
    private int totalCount;

}
