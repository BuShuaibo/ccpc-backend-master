package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetAllSponsorRequest {
    int pageNow;
    int pageSize;
    String keywordName;
    List<Integer> keywordTypes;
    List<Boolean> keywordShows;
}
