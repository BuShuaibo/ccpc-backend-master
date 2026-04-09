package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class AdminGetAllSchoolRequest {
    int pageNow;
    int pageSize;
    String sortType;
    String keywordName;
    String keywordNameEn;
    String keywordMailingAddress;
}
