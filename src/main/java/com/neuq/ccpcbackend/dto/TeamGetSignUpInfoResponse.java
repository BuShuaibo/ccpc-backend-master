package com.neuq.ccpcbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author MachiskComic
 * @ClassName TeamGetSignUpInfoResponse
 * @date 2025-04-24 17:23
 */
@Data
@Builder
public class TeamGetSignUpInfoResponse {
    //队伍id
    String id;
    //队伍名称
    String name;
    //队伍类型
    String teamType;
    //队长ID
    String leaderId;
    //队伍成员ID
    List<String> memberIds;
}
