package com.neuq.ccpcbackend.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SignupUpdateTeamMemberRequest {

    private String coachId;

    @NotBlank(message = "报名ID不能为空")
    private String signupId;

    @NotBlank(message = "队员ID不能为空")
    private String memberId;

    @NotBlank(message = "队员姓名不能为空")
    private String memberName;

    @NotBlank(message = "队员英文名不能为空")
    private String memberNameEn;
}
