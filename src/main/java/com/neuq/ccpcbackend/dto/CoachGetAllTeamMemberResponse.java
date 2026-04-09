package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoachGetAllTeamMemberResponse {
    List<Member> Members;
    @Data
    public static class Member{
        String id;
        String name;
    }
}
