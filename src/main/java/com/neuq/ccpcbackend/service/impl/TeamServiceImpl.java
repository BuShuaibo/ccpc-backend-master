package com.neuq.ccpcbackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.entity.Team;
import com.neuq.ccpcbackend.entity.TeamMember;
import com.neuq.ccpcbackend.entity.TeamType;
import com.neuq.ccpcbackend.entity.User;
import com.neuq.ccpcbackend.mapper.TeamMapper;
import com.neuq.ccpcbackend.mapper.TeamMemberMapper;
import com.neuq.ccpcbackend.mapper.TeamTypeMapper;
import com.neuq.ccpcbackend.mapper.UserMapper;
import com.neuq.ccpcbackend.properties.KeyProperties;
import com.neuq.ccpcbackend.service.TeamService;
import com.neuq.ccpcbackend.utils.JwtUtil;
import com.neuq.ccpcbackend.utils.LambdaQueryWrapperExtension;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.response.Response;
import com.neuq.ccpcbackend.utils.RoleUtil;
import com.neuq.ccpcbackend.vo.TeamVo;
import com.neuq.ccpcbackend.vo.UserAllSchoolCoachVo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Service
@ExtensionMethod({LambdaQueryWrapperExtension.class, LambdaQueryWrapper.class})
public class TeamServiceImpl implements TeamService {
    @Resource
    TeamMapper teamMapper;

    @Resource
    TeamMemberMapper teamMemberMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private TeamTypeMapper teamTypeMapper;

    @Override
    public @Nonnull TeamQueryResponse queryTeam(
        @Nonnull TeamQueryRequest request,
        @Nonnull Consumer<LambdaQueryWrapper<Team>> additionalConstraint
    ) {
        val queryWrapper = queryWrapperOf(request);
        additionalConstraint.accept(queryWrapper);
        val count = teamMapper.selectCount(queryWrapper);
        val page = new Page<Team>(request.getPageNow(), request.getPageSize());
        queryWrapper.select(
            Team::getId,
            Team::getName,
            Team::getNameEn,
            Team::getSchoolId,
            Team::getCoachId,
            Team::getLeaderId,
            Team::getSeason,
            Team::getPrizeCoachId,
            Team::getIsFemaleTeam,
            Team::getTeamTypeId
        );
        val teamPage = teamMapper.selectPage(page, queryWrapper);
        val teams = teamPage.getRecords();
        return new TeamQueryResponse(
            count,
            teams.stream().map(this::getTeamQueryResult).toList()
        );
    }

    @Nonnull
    @Override
    public List<String> getTeamIdsOfStudent(
        @Nonnull String studentId
    ) {
        val queryWrapper = new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getMemberId, studentId)
            .select(TeamMember::getTeamId);
        return teamMemberMapper.selectList(queryWrapper)
            .stream()
            .map(TeamMember::getTeamId)
            .toList();
    }

    private @Nonnull TeamQueryResult getTeamQueryResult(
        @Nonnull Team team
    ) {
        val members = getMemberIds(team).stream().map(this::getTeamQueryResultMember).toList();
        val prizeCoach = getTeamQueryResultMember(team.getPrizeCoachId());
        val result = new TeamQueryResult();
        BeanUtils.copyProperties(team, result);
        result.setMembers(members);
        result.setPrizeCoach(prizeCoach);

        // Set teamType field
        if (team.getTeamTypeId() != null && !team.getTeamTypeId().isBlank()) {
            val teamTypeQueryWrapper = new LambdaQueryWrapper<TeamType>()
                .eq(TeamType::getId, team.getTeamTypeId())
                .select(TeamType::getTypeName);
            val teamType = teamTypeMapper.selectOne(teamTypeQueryWrapper);
            if (teamType != null) {
                result.setTeamType(teamType.getTypeName());
            }
        }

        return result;
    }

    private @Nullable TeamQueryResultMember getTeamQueryResultMember(
        @Nullable String userId
    ) {
        if (userId == null) {
            return null;
        }
        val user = userMapper.selectById(userId);
        val teamQueryResultMember = new TeamQueryResultMember();
        BeanUtils.copyProperties(user, teamQueryResultMember);
        return teamQueryResultMember;
    }

    private @Nonnull List<String> getMemberIds(
        @Nonnull Team team
    ) {
        val queryWrapper = new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getTeamId, team.getId())
            .select(TeamMember::getMemberId);
        return teamMemberMapper.selectList(queryWrapper)
            .stream()
            .map(TeamMember::getMemberId)
            .toList();
    }

    private @Nonnull LambdaQueryWrapper<Team> queryWrapperOf(
        @Nonnull TeamQueryRequest request
    ) {
        val queryWrapper = new LambdaQueryWrapper<Team>();
        if (request.getTeamName() != null && !request.getTeamName().isBlank()) {
            queryWrapper.like(Team::getName, request.getTeamName());
        }
        if (request.getTeamNameEn() != null && !request.getTeamNameEn().isBlank()) {
            queryWrapper.like(Team::getNameEn, request.getTeamNameEn());
        }
        if (request.getTeamMemberNameIn() != null && !request.getTeamMemberNameIn().isBlank()) {
            val userIdList = queryUserIdByNameLikely(request.getTeamMemberNameIn());
            if (!userIdList.isEmpty()) {
                val teamMemberQueryWrapper = new LambdaQueryWrapper<TeamMember>()
                    .in(TeamMember::getMemberId, userIdList)
                    .select(TeamMember::getTeamId);
                val teamIds = teamMemberMapper.selectList(teamMemberQueryWrapper)
                    .stream()
                    .map(TeamMember::getTeamId)
                    .toList();
                queryWrapper.inIfNotEmpty(Team::getId, teamIds);
            }
        }
        if (request.getCoachName() != null && !request.getCoachName().isBlank()) {
            val userIdList = queryUserIdByNameLikely(request.getCoachName());
            queryWrapper.inIfNotEmpty(Team::getCoachId, userIdList);
        }
        if (request.getLeaderName() != null && !request.getLeaderName().isBlank()) {
            val userIdList = queryUserIdByNameLikely(request.getLeaderName());
            queryWrapper.inIfNotEmpty(Team::getLeaderId, userIdList);
        }
        if (request.getIsFemaleTeam() != null) {
            queryWrapper.eq(Team::getIsFemaleTeam, request.getIsFemaleTeam());
        }
        if (request.getSeason() != null && !request.getSeason().isBlank()) {
            queryWrapper.like(Team::getSeason, request.getSeason());
        }
        if (request.getTeamType() != null && !request.getTeamType().isBlank()) {
            val teamTypeQueryWrapper = new LambdaQueryWrapper<TeamType>()
                .eq(TeamType::getTypeName, request.getTeamType())
                .select(TeamType::getId);
            val teamType = teamTypeMapper.selectOne(teamTypeQueryWrapper);
            if (teamType != null) {
                queryWrapper.eq(Team::getTeamTypeId, teamType.getId());
            }
        }
        if (request.getSortedBy() != null) {
            queryWrapper.orderBy(true, request.getIsAsc(), request.getSortedBy().field);
        }
        return queryWrapper;
    }

    private @Nonnull List<String> queryUserIdByNameLikely(@Nonnull String name) {
        val coachQueryWrapper = new LambdaQueryWrapper<User>()
            .like(User::getFirstName, name)
            .or()
            .like(User::getLastName, name)
            .or()
            .apply("concat(first_name, last_name) like {0}", name)
            .or()
            .apply("concat(last_name, first_name) like {0}", name)
            .select(User::getId);
        return userMapper.selectList(coachQueryWrapper)
            .stream()
            .map(User::getId)
            .toList();
    }

    @Override
    public Response generateTeamInviteToken(String teamId, Long seconds) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getId, teamId)
                .select(Team::getId, Team::getCoachId, Team::getLeaderId);
        Team team = teamMapper.selectOne(queryWrapper);
        if (team == null) {
            return Response.error(ErrorCode.GENERATE_INVITE_FAILED.getErrCode(), "队伍不存在");
        }

        if (RoleUtil.hasRole("coach") && id.equals(team.getCoachId()) ||
                RoleUtil.hasRole("contest") && id.equals(team.getLeaderId())) {

            // 先作废旧的邀请链接
            String preTeamInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.TEAM_INVITE_TOKEN_PREFIX + teamId);
            if (preTeamInviteToken != null) {
                redisTemplate.delete(KeyProperties.TEAM_INVITE_TOKEN_PREFIX + teamId);
            }

            Map<String, String> map = new HashMap<>();
            map.put("type", "teamInviteToken");
            map.put("teamId", teamId);
            map.put("createUserId", id);
            String teamInviteToken = JwtUtil.generateToken(JSON.toJSONString(map), seconds * 1000L);
            redisTemplate.opsForValue().set(KeyProperties.TEAM_INVITE_TOKEN_PREFIX + teamId, teamInviteToken, seconds, TimeUnit.SECONDS);
            return Response.of(teamInviteToken);
        } else if (RoleUtil.hasRole("coach")) {
            return Response.error(ErrorCode.ACCESS_DENIED.getErrCode(), "您并非该队伍教练，无权管理该队伍");
        } else if (RoleUtil.hasRole("contest")) {
            return Response.error(ErrorCode.ACCESS_DENIED.getErrCode(), "您并非该队伍队长，无权管理该队伍");
        } else {
            return Response.error(ErrorCode.ACCESS_DENIED.getErrCode(), "您无权管理该队伍");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response acceptTeamInvite(String token) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 解析token中的数据
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (ExpiredJwtException e) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "队伍邀请链接已过期");
        } catch (Exception e) {
            return Response.error(ErrorCode.TOKEN_PARSE_ERROR.getErrCode(), "队伍邀请链接解析失败");
        }
        Map<String, String> map = JSON.parseObject(claims.getSubject(), new TypeReference<Map<String, String>>() {});
        String type = map.get("type");
        String teamId = map.get("teamId");
        if (type == null || teamId == null) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
        }
        if (!type.equals("teamInviteToken")) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "该token无法用作加入团队");
        }

        // 看是否失效
        String preTeamInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.TEAM_INVITE_TOKEN_PREFIX + teamId);
        if (preTeamInviteToken == null || !preTeamInviteToken.equals(token)) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "token已失效");
        }

        LambdaQueryWrapper<TeamMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeamMember::getTeamId, teamId).eq(TeamMember::getMemberId, id);
        TeamMember teamMemberExist = teamMemberMapper.selectOne(queryWrapper);
        if (teamMemberExist != null) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "您已经在此队伍中");
        }

        TeamMember teamMember = new TeamMember();
        teamMember.setId(UUID.randomUUID().toString());
        teamMember.setMemberId(id);
        teamMember.setTeamId(teamId);
        teamMemberMapper.insert(teamMember);



        /// //第一个加入的人是队长
        LambdaQueryWrapper<TeamMember> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(TeamMember::getTeamId, teamId);
        Long memberCount = teamMemberMapper.selectCount(countWrapper);
        // 查询队伍信息
        Team team = teamMapper.selectById(teamId);
        //设置属性isLeader，如果未执行该查询，isLeader为false
        boolean isLeader = false;
        if (memberCount ==  1 && (team.getLeaderId() == null || team.getLeaderId().equals("")) ){

            // 更新队长信息
            team.setLeaderId(id);
            teamMapper.updateById(team);
            isLeader = true;
        }
        // 构造返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("isLeader", isLeader);
        return Response.of(data);
    }

    @Override
    public Response generateCoachTeamInviteToken(Long seconds) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 先作废旧的邀请链接
        String preCoachTeamInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.COACH_TEAM_INVITE_TOKEN_PREFIX + id);
        if (preCoachTeamInviteToken != null) {
            redisTemplate.delete(KeyProperties.COACH_TEAM_INVITE_TOKEN_PREFIX + id);
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).select(User::getId, User::getSchoolId);
        User user = userMapper.selectOne(queryWrapper);
        Map<String, String> map = new HashMap<>();
        map.put("type", "coachTeamInviteToken");
        map.put("schoolId", user.getSchoolId());
        map.put("coachId", id);
        map.put("createUserId", id);
        String coachTeamInviteToken = JwtUtil.generateToken(JSON.toJSONString(map), seconds * 1000L);
        redisTemplate.opsForValue().set(KeyProperties.COACH_TEAM_INVITE_TOKEN_PREFIX + id, coachTeamInviteToken, seconds, TimeUnit.SECONDS);
        return Response.of(coachTeamInviteToken);
    }

    @Override
    public Response acceptCoachTeamInvite(String teamId, String token) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 队长校验
        LambdaQueryWrapper<Team> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Team::getId, teamId)
                .select(Team::getLeaderId);
        Team team = teamMapper.selectOne(lambdaQueryWrapper);
        if (team == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "该队伍不存在");
        }
        if (!id.equals(team.getLeaderId())) {
            return Response.error(ErrorCode.ACCESS_DENIED.getErrCode(), "您并非该队伍队长，无权管理该队伍");
        }

        // 解析token中的数据
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (ExpiredJwtException e) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "教练队伍邀请链接已过期");
        } catch (Exception e) {
            return Response.error(ErrorCode.TOKEN_PARSE_ERROR.getErrCode(), "教练队伍邀请链接解析失败");
        }
        Map<String, String> map = JSON.parseObject(claims.getSubject(), new TypeReference<Map<String, String>>() {});
        String type = map.get("type");
        String schoolId = map.get("schoolId");
        String coachId = map.get("coachId");
        String createUserId = map.get("createUserId");
        if (type == null || schoolId == null || coachId == null || createUserId == null) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
        }

        if (!type.equals("coachTeamInviteToken")) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "该token无法用作团队加入教练麾下");
        }

        // 看是否失效
        String preCoachTeamInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.COACH_TEAM_INVITE_TOKEN_PREFIX + createUserId);
        if (preCoachTeamInviteToken == null || !preCoachTeamInviteToken.equals(token)) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "token已失效");
        }

        LambdaUpdateWrapper<Team> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Team::getId, teamId)
                .set(Team::getSchoolId, schoolId)
                .set(Team::getCoachId, coachId);
        int result = teamMapper.update(updateWrapper);
        if (result == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "团队加入教练麾下失败");
        }
    }

    @Override
    public Response addCoachTeam(TeamAddCoachTeamRequest request) {
        // 获取自己的id
        String id = RoleUtil.getCurrentUserId();

        // 检查用户是否有教练角色
        if (!RoleUtil.hasRole("coach") && !RoleUtil.hasRole("admin")) {
            return Response.error(ErrorCode.ACCESS_DENIED.getErrCode(), "您不是教练，无法创建队伍");
        }

        // 获取用户的学校ID
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getId, id).select(User::getSchoolId);
        User user = userMapper.selectOne(userQueryWrapper);
        if (user == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "用户不存在");
        }
        String schoolId = user.getSchoolId();

        //自动设置赛年
        String season = request.getSeason();
        if (season == null || season.isEmpty()) {
            season = LocalDate.now().getYear() + "";
        }

        // 创建新队伍
        Team team = new Team();
        team.setId(UUID.randomUUID().toString());
        team.setName(request.getName());
        team.setNameEn(request.getNameEn());
        team.setSchoolId(schoolId);
        team.setCoachId(id);
        team.setSeason(season);
        team.setPrizeCoachId(request.getPrizeCoachId());
        team.setIsFemaleTeam(request.getIsFemaleTeam());
        team.setLeaderId(request.getLeaderId());
        team.setTeamTypeId(request.getTeamTypeId());
        team.setQuotaRest((long) request.getQuotaRest());

        // 保存队伍
        int result = teamMapper.insert(team);
        if (result != 1) {
            return Response.error(ErrorCode.INSERT_FAILED.getErrCode(), "创建队伍失败");
        }

        // 如果有队员ID列表，添加队员
        String membersIds = request.getMembersIds();
        if (membersIds != null && !membersIds.isEmpty()) {
            String[] memberIdArray = membersIds.split(",");
            for (String memberId : memberIdArray) {
                if (memberId != null && !memberId.trim().isEmpty()) {
                    TeamMember teamMember = new TeamMember();
                    teamMember.setId(UUID.randomUUID().toString());
                    teamMember.setTeamId(team.getId());
                    teamMember.setMemberId(memberId.trim());
                    int memberInsertResult = teamMemberMapper.insert(teamMember);
                    if (memberInsertResult <= 0) {
                        return Response.error(ErrorCode.INSERT_FAILED.getErrCode(), "添加队员失败");
                    }
                }
            }
        }

        return Response.of(team.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务注解，任何异常都会回滚
    public Response addStudentTeam(TeamAddStudentTeamRequest teamAddStudentTeamRequest) {
        String name = teamAddStudentTeamRequest.getName();
        String name_en = teamAddStudentTeamRequest.getNameEn();
        String schoolId = teamAddStudentTeamRequest.getSchoolId();
        String coachId = teamAddStudentTeamRequest.getCoachId();
        String leaderId = RoleUtil.getCurrentUserId();
        String season = LocalDate.now().getYear() + "";
        String prizeCoachId = teamAddStudentTeamRequest.getPrizeCoachId();
        Boolean isFemaleTeam = teamAddStudentTeamRequest.getIsFemaleTeam();
        String teamTypeId = teamAddStudentTeamRequest.getTeamTypeId();
        Long quotaRest = teamAddStudentTeamRequest.getQuotaRest();

        //同一个赛季队伍名不能重复
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getSeason,season)
                .eq(Team::getName,name)
                .eq(Team::getNameEn,name_en);
        Team teamValidation = teamMapper.selectOne(queryWrapper);
        if (teamValidation != null) {
            return Response.error(ErrorCode.DATA_EXIST.getErrCode(),"该队伍名已经存在！");
        }

        //添加队伍
        Team team = new Team();
        team.setId(UUID.randomUUID().toString());
        team.setName(name);
        team.setNameEn(name_en);
        team.setSchoolId(schoolId);
        team.setCoachId(coachId);
        team.setLeaderId(leaderId);
        team.setSeason(season);
        team.setPrizeCoachId(prizeCoachId);
        team.setIsFemaleTeam(isFemaleTeam);
        team.setTeamTypeId(teamTypeId);
        team.setQuotaRest(quotaRest);
        int teamInsertResult = teamMapper.insert(team);
        if (teamInsertResult <= 0) {
            throw new RuntimeException("创建队伍失败"); // 抛出异常触发事务回滚
        }
        //队长入队
        TeamMember teamMember = new TeamMember();
        teamMember.setId(UUID.randomUUID().toString());
        teamMember.setTeamId(team.getId());
        teamMember.setMemberId(leaderId);
        int memberInsertResult = teamMemberMapper.insert(teamMember);
        if (memberInsertResult <= 0) {
            throw new RuntimeException("添加队长到队伍失败"); // 抛出异常触发事务回滚
        }
        return Response.of(team.getId());
    }

    @Override
    public Response getTeamInfo(String teamId) {
        //能够直接看懂的数据
        TeamVo teamVo = teamMapper.getTeamInfo(teamId);
        //队伍的抽象数据
        Team team = teamMapper.selectById(teamId);
        if(team.getCoachId() != null && !team.getCoachId().isEmpty()){
            teamVo.setCoachName(teamVo.getCoachFirstName()+teamVo.getCoachLastName());
        }
        if(team.getPrizeCoachId() != null && !team.getPrizeCoachId().isEmpty()){
            teamVo.setPrizeCoachName(teamVo.getPrizeCoachFirstName()+teamVo.getPrizeCoachLastName());
            teamVo.setPrizeCoachNameEn(teamVo.getPrizeCoachFirstNameEn()+teamVo.getPrizeCoachLastNameEn());
        }
        teamVo.setMembers(teamMapper.getMember(teamId));
        teamVo.setTeam(team);
        return Response.of(teamVo);
    }

    @Override
    public Response getAllSchoolCoach(Integer pageNum, Integer pageSize, String schoolId, UserKeywordRequest userKeywordRequest) {
        // 创建分页对象
        Page<User> page = new Page<>(pageNum, pageSize);
        UserAllSchoolCoachVo userAllSchoolCoachVo = new UserAllSchoolCoachVo();
        Page<User> allSchoolCoach = userMapper.getAllSchoolCoach(page, schoolId , userKeywordRequest);
        userAllSchoolCoachVo.setCoachList(allSchoolCoach.getRecords());
        userAllSchoolCoachVo.setPageTotal(allSchoolCoach.getTotal());
        userAllSchoolCoachVo.setPages(allSchoolCoach.getPages());
        return Response.of(userAllSchoolCoachVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response updateTeam(TeamUpdateTeamRequest teamUpdateTeamRequest){
        String currentUserId = RoleUtil.getCurrentUserId();
        String id = teamUpdateTeamRequest.getId();
        String name = teamUpdateTeamRequest.getName();
        String name_en = teamUpdateTeamRequest.getNameEn();
        String leaderId = teamUpdateTeamRequest.getLeaderId();
        String season = teamUpdateTeamRequest.getSeason();
        String prizeCoachId = teamUpdateTeamRequest.getPrizeCoachId();
        Boolean isFemaleTeam = teamUpdateTeamRequest.getIsFemaleTeam();
        Long quotaRest = teamUpdateTeamRequest.getQuotaRest();
        //判断有无权限
        Team team1 = teamMapper.selectById(id);
        if (!currentUserId.equals(team1.getLeaderId()) && !currentUserId.equals(team1.getCoachId())) {
            return Response.error(ErrorCode.UPDATE_FAILED.getErrCode(), "你没有权限");
        }
        //对team表直接更新的部分
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        team.setNameEn(name_en);
        team.setLeaderId(leaderId);
        team.setPrizeCoachId(prizeCoachId);
        team.setIsFemaleTeam(isFemaleTeam);
        team.setQuotaRest(quotaRest);
        int updateByIdResult = teamMapper.updateById(team);
        if (updateByIdResult < 0) {
            throw new RuntimeException("更新队伍失败！");
        }
        //教练对队员进行增删，修改赛季
        if(RoleUtil.hasRole("coach")){
            Team team2 = new Team();
            team2.setId(id);
            team2.setSeason(season);
            int updateSeasonByIdResult = teamMapper.updateById(team2);
            if (updateSeasonByIdResult < 0) {
                throw new RuntimeException("更新队伍失败！");
            }
            List<String> deleteMembers = new ArrayList<>(teamUpdateTeamRequest.getOldMembers());
            deleteMembers.removeAll(teamUpdateTeamRequest.getNewMembers());
            if(!deleteMembers.isEmpty()){
                LambdaQueryWrapper<TeamMember> queryWrapperDelete = new LambdaQueryWrapper<>();
                queryWrapperDelete.eq(TeamMember::getTeamId, id).in(TeamMember::getMemberId, deleteMembers);
                int deleteResult = teamMemberMapper.delete(queryWrapperDelete);
                if (deleteResult < 0) {
                    throw new RuntimeException("更新队伍失败！");
                }
            }
            List<String> addMembers = new ArrayList<>(teamUpdateTeamRequest.getNewMembers());
            addMembers.removeAll(teamUpdateTeamRequest.getOldMembers());
            if(!addMembers.isEmpty()){
                for(String memberId : addMembers){
                    TeamMember teamMember = new TeamMember();
                    teamMember.setId(UUID.randomUUID().toString());
                    teamMember.setMemberId(memberId);
                    teamMember.setTeamId(id);
                    int memberInsertResult = teamMemberMapper.insert(teamMember);
                    if (memberInsertResult < 0) {
                        throw new RuntimeException("更新队伍失败！");
                    }
                }
            }
        }
        return Response.success();
    }

    @Override
    public Response getTeamInviteToken(String teamId) {
        Long teamInviteExpire = redisTemplate.getExpire(KeyProperties.TEAM_INVITE_TOKEN_PREFIX + teamId, TimeUnit.SECONDS);
        String teamInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.TEAM_INVITE_TOKEN_PREFIX + teamId);
        TeamGetTeamInviteToken teamGetTeamInviteToken = new TeamGetTeamInviteToken();
        teamGetTeamInviteToken.setTeamInviteToken(teamInviteToken);
        teamGetTeamInviteToken.setTeamInviteExpire(teamInviteExpire);
        return Response.of(teamGetTeamInviteToken);
    }

    @Override
    public Response getAllTeamType() {
        List<TeamType> teamTypes = teamTypeMapper.selectList(null);
        return Response.of(teamTypes);
    }

}
