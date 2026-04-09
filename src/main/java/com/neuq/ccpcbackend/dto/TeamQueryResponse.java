package com.neuq.ccpcbackend.dto;

import lombok.Value;

import java.util.List;

@Value
public class TeamQueryResponse {
    long count;
    List<TeamQueryResult> teams;
}
