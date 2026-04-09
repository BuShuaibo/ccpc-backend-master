package com.neuq.ccpcbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.entity.Team;
import com.neuq.ccpcbackend.mapper.TeamMapper;
import com.neuq.ccpcbackend.service.TeamService;
import com.neuq.ccpcbackend.utils.LambdaQueryWrapperExtension;
import com.neuq.ccpcbackend.utils.RoleUtil;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@ExtensionMethod({LambdaQueryWrapperExtension.class, LambdaQueryWrapper.class})
@RestController
@RequestMapping("/api/team")
public class TeamController {
    @Resource
    TeamService teamService;
    @Autowired
    private TeamMapper teamMapper;

    // 教练查询所有属于自己的队伍
    @PostMapping("/queryCoachTeam")
    @PreAuthorize("hasAnyAuthority('admin', 'coach')")
    public Response queryCoachTeam(@RequestBody TeamQueryRequest request) {
        String id = RoleUtil.getCurrentUserId();
        val response = teamService.queryTeam(request, queryWrapper -> queryWrapper.eq(Team::getCoachId, id));
        return Response.of(response);
    }

    @GetMapping("/generateTeamInviteToken")
    @PreAuthorize("hasAnyAuthority('coach', 'contest')")
    public Response generateTeamInviteToken(@RequestParam String teamId, @RequestParam Long seconds) {
        return teamService.generateTeamInviteToken(teamId, seconds);
    }

    @GetMapping("/acceptTeamInvite")
    @PreAuthorize("hasAnyAuthority('contest')")
    public Response acceptTeamInvite(@RequestParam String token) {
        return teamService.acceptTeamInvite(token);
    }

    @GetMapping("/generateCoachTeamInviteToken")
    @PreAuthorize("hasAnyAuthority('coach', 'contest')")
    public Response generateCoachTeamInviteToken(@RequestParam Long seconds) {
        return teamService.generateCoachTeamInviteToken(seconds);
    }

    @GetMapping("/acceptCoachTeamInvite")
    @PreAuthorize("hasAnyAuthority('contest')")
    public Response acceptCoachTeamInvite(@RequestParam String teamId, @RequestParam String token) {
        return teamService.acceptCoachTeamInvite(teamId, token);
    }


    // 学生查询所有属于自己的队伍
    @PostMapping("/queryStudentTeam")
    @PreAuthorize("hasAnyAuthority('admin', 'contest')")
    public Response queryStudentTeam(@RequestBody TeamQueryRequest request) {
        val id = RoleUtil.getCurrentUserId();
        val teamIds = teamService.getTeamIdsOfStudent(id);
        val response = teamService.queryTeam(request, queryWrapper -> queryWrapper.inIfNotEmpty(Team::getId, teamIds));
        return Response.of(response);
    }

    //TODO 创建队伍需要加上类型选择
    // 教练创建队伍
    @PostMapping("/addCoachTeam")
    @PreAuthorize("hasAnyAuthority('admin', 'coach')")
    public Response addCoachTeam(@RequestBody TeamAddCoachTeamRequest request) {
        return teamService.addCoachTeam(request);
    }


    //学生创建队伍
    @PostMapping("/addStudentTeam")
    @PreAuthorize("hasAnyAuthority('contest')")
    public Response addStudentTeam(@RequestBody TeamAddStudentTeamRequest teamAddStudentTeamRequest) {
        return teamService.addStudentTeam(teamAddStudentTeamRequest);
    }

    //获取单个队伍信息
    @GetMapping("/getTeamInfo")
    @PreAuthorize("hasAnyAuthority('admin', 'contest', 'coach', 'schoolAdmin')")
    public Response getTeamInfo(@RequestParam String teamId) {
        return teamService.getTeamInfo(teamId);
    }

    //获取学校的全部教练
    @PostMapping("/getAllSchoolCoach")
    @PreAuthorize("hasAnyAuthority('admin', 'contest', 'coach', 'schoolAdmin')")
    public Response getAllSchoolCoach(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize
                                    , @RequestParam String schoolId,
                                      @RequestBody UserKeywordRequest userKeywordRequest) {
        return teamService.getAllSchoolCoach(pageNum,pageSize,schoolId, userKeywordRequest);
    }

    //更新队伍信息
    @PostMapping("/updateTeam")
    @PreAuthorize("hasAnyAuthority('contest', 'coach', 'schoolAdmin')")
    public Response updateTeam(@RequestBody TeamUpdateTeamRequest teamAddStudentTeamRequest){
        return teamService.updateTeam(teamAddStudentTeamRequest);
    }

    //获取队伍当前的队伍邀请链接
    @GetMapping("/getTeamInviteToken")
    @PreAuthorize("hasAnyAuthority('coach', 'contest')")
    public Response getTeamInviteToken(@RequestParam String teamId) {
        return teamService.getTeamInviteToken(teamId);
    }

    //查看所有队伍类型
    @GetMapping("/getAllTeamType")
    @PermitAll()
    public Response getAllTeamType() {
        return teamService.getAllTeamType();
    }
}
