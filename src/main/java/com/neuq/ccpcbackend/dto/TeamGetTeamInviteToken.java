package com.neuq.ccpcbackend.dto;

import lombok.Data;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Data
public class TeamGetTeamInviteToken {
    String teamInviteToken;
    Long teamInviteExpire;
}
