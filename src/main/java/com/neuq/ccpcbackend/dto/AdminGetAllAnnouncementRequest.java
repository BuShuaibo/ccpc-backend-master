package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetAllAnnouncementRequest {
    int pageNow;
    int pageSize;
    String sortType;
    String keywordAuthor;
    String keywordTitle;
    List<Integer> keywordStatuses;
    List<String> keywordSeasons;
}
