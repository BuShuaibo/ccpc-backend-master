package com.neuq.ccpcbackend.dto;

import com.neuq.ccpcbackend.entity.TeamSortField;
import lombok.Data;

@Data
public class TeamQueryRequest {
    private int pageNow;
    private int pageSize;
    private String teamName;
    private String teamNameEn;
    private String teamMemberNameIn;
    private String coachName;
    private String leaderName;
    private Boolean isFemaleTeam;
    private String season;
    private TeamSortField sortedBy;
    private Boolean isAsc;
    private String teamType;
}
