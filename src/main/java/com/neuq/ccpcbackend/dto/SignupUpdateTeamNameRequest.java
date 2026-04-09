package com.neuq.ccpcbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupUpdateTeamNameRequest {

    String coachId;

    @NotBlank(message = "报名ID不能为空")
    String signupId;


    @NotBlank(message = "队伍名称不能为空")
    String teamNameAfter;
    @NotBlank(message = "队伍英文名称不能为空")
    String teamNameEnAfter;
}
