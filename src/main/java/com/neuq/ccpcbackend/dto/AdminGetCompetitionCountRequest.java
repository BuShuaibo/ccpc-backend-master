package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetCompetitionCountRequest {
    String keywordName;
    String keywordSchool;
    String keywordCompetitionAdmin;
    List<String> keywordTypes;
    List<String> keywordSeasons;
}
