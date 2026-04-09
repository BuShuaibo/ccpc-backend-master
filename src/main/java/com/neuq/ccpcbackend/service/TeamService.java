package com.neuq.ccpcbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.entity.Team;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public interface TeamService {
    @Nonnull TeamQueryResponse queryTeam(@Nonnull TeamQueryRequest request, @Nonnull Consumer<LambdaQueryWrapper<Team>> additionalConstraint);
    @Nonnull List<String> getTeamIdsOfStudent(@Nonnull String studentId);
    Response generateTeamInviteToken(String teamId, Long seconds);
    Response acceptTeamInvite(String token);
    Response generateCoachTeamInviteToken(Long seconds);
    Response acceptCoachTeamInvite(String teamId, String token);
    Response addCoachTeam(TeamAddCoachTeamRequest request);
    Response addStudentTeam(TeamAddStudentTeamRequest teamAddStudentTeamRequest);
    Response getTeamInfo(String teamId);
    Response getAllSchoolCoach(Integer pageNum, Integer pageSize, String schoolId, UserKeywordRequest userKeywordRequest);
    Response updateTeam(TeamUpdateTeamRequest teamUpdateTeamRequest);
    Response getTeamInviteToken(String teamId);
    Response getAllTeamType();
}
