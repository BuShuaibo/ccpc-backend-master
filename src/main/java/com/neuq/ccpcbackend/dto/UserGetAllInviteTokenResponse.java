package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class UserGetAllInviteTokenResponse {
    String coachInviteToken;
    Long coachInviteExpire;

    String coachTeamInviteToken;
    Long coachTeamInviteExpire;

    String schoolInviteToken;
    Long schoolInviteExpire;
}
