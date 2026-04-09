package com.neuq.ccpcbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.entity.*;
import com.neuq.ccpcbackend.mapper.*;
import com.neuq.ccpcbackend.service.SignupService;
import com.neuq.ccpcbackend.utils.RoleUtil;
import com.neuq.ccpcbackend.utils.TimeUtil;
import com.neuq.ccpcbackend.utils.constant.OccupationQuotaTypeConstant;
import com.neuq.ccpcbackend.utils.constant.SignupStatusConstant;
import com.neuq.ccpcbackend.utils.exception.BizException;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class SignupServiceImpl implements SignupService {

    @Resource
    SignupChangeRequestMapper signupChangeRequestMapper;
    @Resource
    SignupMapper signupMapper;
    @Resource
    SignupMemberMapper signupMemberMapper;
    @Resource
    CompetitionMapper competitionMapper;
    @Resource
    TeamMapper teamMapper;
    @Resource
    SchoolMapper schoolMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    TeamMemberMapper teamMemberMapper;
    @Resource
    private QuotaDistributionMapper quotaDistributionMapper;

    private String getUserById(String userId)
    {
        User user = userMapper.selectById(userId);
        if (user == null) {
            System.out.println("用户ID为 {} 的用户未找到"+ userId);
            return null; // 或者抛一个业务异常，例如 throw new BusinessException("用户不存在")
        }
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        return firstName+lastName;
    }


    // todo 加筛选条件
    @Override
    public Response getAllCompetitionAdminSignupChangeRequest(int pageNow,int pageSize,String keyword,Integer status) {

        LambdaQueryWrapper<SignupChangeRequest> queryWrapper = new LambdaQueryWrapper<>();

        if(keyword!=null)
        {
            queryWrapper.like(SignupChangeRequest::getSignupId,keyword);
        }

        if(status!=null)
        {
            queryWrapper.eq(SignupChangeRequest::getRequestStatus,status);
        }

        //获取管理员管理所有比赛
        String userId = RoleUtil.getCurrentUserId();
        LambdaQueryWrapper<Competition> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Competition::getCompetitionAdminId,userId);
        List<Competition> competitions = competitionMapper.selectList(queryWrapper1);

        //获取管理比赛所有报名信息
        List<Signup> signups = new ArrayList<>();
        for(Competition competition : competitions)
        {
            LambdaQueryWrapper<Signup> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(Signup::getCompetitionId,competition.getId());
            signups.addAll(signupMapper.selectList(queryWrapper2));
        }
        queryWrapper.in(SignupChangeRequest::getSignupId,signups.stream().map(Signup::getId).toList());

        Page<SignupChangeRequest> page = new Page<>(pageNow,pageSize);
        Page<SignupChangeRequest> signupChangeRequestPage =signupChangeRequestMapper.selectPage(page,queryWrapper);
        List<SignupChangeRequest> records = signupChangeRequestPage.getRecords();

        List<AdminGetSignupChangeRequestResponse> ans = new ArrayList<>();
        for(SignupChangeRequest signupChangeRequest : records)
        {
            AdminGetSignupChangeRequestResponse adminGetSignupChangeRequestResponse = new AdminGetSignupChangeRequestResponse();
            BeanUtils.copyProperties(signupChangeRequest,adminGetSignupChangeRequestResponse);
            String signupId = signupChangeRequest.getSignupId();
            Signup signup = signupMapper.selectById(signupId);
            String competitionName = competitionMapper.selectById(signup.getCompetitionId()).getName();
            adminGetSignupChangeRequestResponse.setCompetitionName(competitionName);
            String leaderIdBefore =signupChangeRequest.getLeaderIdBefore();
            if(leaderIdBefore!=null) adminGetSignupChangeRequestResponse.setLeaderNameBefore(getUserById(leaderIdBefore));
            String leaderIdAfter = signupChangeRequest.getLeaderIdAfter();
            if(leaderIdAfter!=null) adminGetSignupChangeRequestResponse.setLeaderNameAfter(getUserById(leaderIdAfter));
            String prizeCoachIdBefore = signupChangeRequest.getPrizeCoachIdBefore();
            if(prizeCoachIdBefore!=null) adminGetSignupChangeRequestResponse.setPrizeCoachNameBefore(getUserById(prizeCoachIdBefore));
            String prizeCoachIdAfter = signupChangeRequest.getPrizeCoachIdAfter();
            if(prizeCoachIdAfter!=null) adminGetSignupChangeRequestResponse.setPrizeCoachNameAfter(getUserById(prizeCoachIdAfter));

            List<String> teamMemberBefore = new ArrayList<>();
            LambdaQueryWrapper<SignupMember> queryWrapper3 = new LambdaQueryWrapper<>();
            queryWrapper3.eq(SignupMember::getSignupId,signupChangeRequest.getSignupId());
            List<SignupMember> signupMembers = signupMemberMapper.selectList(queryWrapper3);
            for(SignupMember signupMember : signupMembers)
            {
                String memberId = signupMember.getMemberId();
                if(memberId!=null) teamMemberBefore.add(getUserById(memberId));
            }
            adminGetSignupChangeRequestResponse.setTeamMemberBefore(teamMemberBefore);

            List<String> teamMemberAfter = new ArrayList<>();
            for(SignupMember signupMember : signupMembers)
            {
                String memberId = signupMember.getMemberId();
                if(memberId!=null) teamMemberAfter.add(getUserById(memberId));
            }
            adminGetSignupChangeRequestResponse.setTeamMemberAfter(teamMemberAfter);
            ans.add(adminGetSignupChangeRequestResponse);
        }
        return Response.of(ans);
    }



    @Transactional
    @Override
    public Response getAllCompetitionAdminSignupInfo(int pageNow, int pageSize, Integer status) {
        // 获取管理员管理的所有比赛
        String userId = RoleUtil.getCurrentUserId();
        LambdaQueryWrapper<Competition> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Competition::getCompetitionAdminId,userId);
        List<Competition> competitions = competitionMapper.selectList(queryWrapper1);
        List<String> competitionIds = competitions.stream().map(Competition::getId).toList();

        LambdaQueryWrapper<Signup> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(Signup::getCompetitionId,competitionIds);
        if(status!=null) queryWrapper2.eq(Signup::getProcessStatus,status);
        queryWrapper2.orderByAsc(Signup::getProcessStatus);
        Page<Signup> page = new Page<>(pageNow,pageSize);
        Page<Signup> signupPage = signupMapper.selectPage(page, queryWrapper2);
        List<Signup> records = signupPage.getRecords();

        List<AdminGetAllSignupResponse> ans = new ArrayList<>();
        for(Signup signup : records)
        {
            AdminGetAllSignupResponse adminGetAllSignupResponse = new AdminGetAllSignupResponse();
            BeanUtils.copyProperties(signup,adminGetAllSignupResponse);
            adminGetAllSignupResponse.setSignupId(signup.getId());

            // 获取队伍信息
            String teamId = signup.getTeamId();
            if(teamId!=null && !teamId.isEmpty())
            {
                LambdaQueryWrapper<Team> queryWrapper3 = new LambdaQueryWrapper<>();
                queryWrapper3.eq(Team::getId,teamId);
                Team team = teamMapper.selectOne(queryWrapper3);
                if(team!=null)
                {
                    adminGetAllSignupResponse.setTeamName(team.getName());
                    adminGetAllSignupResponse.setTeamNameEn(team.getNameEn());
                }
            }

            // 获取队长
            String leaderId = signup.getLeaderId();
            adminGetAllSignupResponse.setLeaderName(getUserById(leaderId));

            // 获取队伍成员
            LambdaQueryWrapper<SignupMember> queryWrapper3 = new LambdaQueryWrapper<>();
            queryWrapper3.eq(SignupMember::getSignupId,signup.getId());
            List<SignupMember> signupMembers = signupMemberMapper.selectList(queryWrapper3);
            List<String> teamMember = new ArrayList<>();
            List<String> teamMemberEn = new ArrayList<>();
            for(SignupMember signupMember : signupMembers)
            {
                String memberId = signupMember.getMemberId();
                teamMember.add(getUserById(memberId));
                User user = userMapper.selectById(memberId);
                String memberNameEn =user.getFirstNameEn()+user.getLastNameEn();
                teamMemberEn.add(memberNameEn);
            }
            adminGetAllSignupResponse.setTeamMembersName(teamMember);
            adminGetAllSignupResponse.setTeamMembersNameEn(teamMemberEn);

            // 获取学校
                String schoolId = signup.getSchoolId();
            if(schoolId!=null && !schoolId.isEmpty())
            {
                LambdaQueryWrapper<School> queryWrapper4 = new LambdaQueryWrapper<>();
                queryWrapper4.eq(School::getId,schoolId);
                School school = schoolMapper.selectOne(queryWrapper4);
                if(school!=null)
                {
                    adminGetAllSignupResponse.setSchool(school.getName());
                }
            }

            // 获取教练
            String coachId = signup.getCoachId();
            adminGetAllSignupResponse.setCoach(getUserById(coachId));

            // 获取证书教练
                String prizeCoachId = signup.getPrizeCoachId();
            adminGetAllSignupResponse.setPrizeCoach(getUserById(prizeCoachId));

            // 获取比赛信息
            String competitionId = signup.getCompetitionId();
            if(competitionId!=null && !competitionId.isEmpty())
            {
                LambdaQueryWrapper<Competition> queryWrapper4 = new LambdaQueryWrapper<>();
                queryWrapper4.eq(Competition::getId,competitionId);
                Competition competition = competitionMapper.selectOne(queryWrapper4);
                if(competition!=null)
                {
                    adminGetAllSignupResponse.setCompetition(competition.getName());
                }
            }

            ans.add(adminGetAllSignupResponse);
        }
        AdminGetAllSignupPageResponse adminGetAllSignupListResponse = new AdminGetAllSignupPageResponse();
        adminGetAllSignupListResponse.setSignups(ans);
        adminGetAllSignupListResponse.setTotalCount(signupPage.getTotal());
        return Response.of(adminGetAllSignupListResponse);
    }

    @Override
    public Response updateSignupStatus(String signupId, Integer status) {
        Signup signup = signupMapper.selectById(signupId);
        if(signup==null)
        {
            return Response.error(ErrorCode.DATA_NOT_EXIST);
        }
        Competition competition = competitionMapper.selectById(signup.getCompetitionId());
        int status_after = status;
        if(status==1)
        {
            if(competition.needPaymentReceipt) {
                status_after=2;
            }
            else {
                if(competition.needSignIn)
                {
                    status_after=4;
                }
                else {
                    if(competition.needUploadPicture) status_after=5;
                    else status_after=6;
                }
            }
        }
        if(status==3) status_after=4;

        signup.setProcessStatus(status_after);
        signupMapper.updateById(signup);
        return Response.success();
    }

    @Transactional
    @Override
    public Response auditSignupChangeRequest(AdminAuditSignupChangeRequest request) {
        SignupChangeRequest signupChangeRequest = signupChangeRequestMapper.selectById(request.getId());
        if(signupChangeRequest==null)
        {
            return Response.error(ErrorCode.DATA_NOT_EXIST);
        }
        if(signupChangeRequest.getRequestStatus()!=1)
        {
            return Response.error("411","当前请求已处理");
        }
        signupChangeRequest.setRequestStatus(request.getRequestStatus());
        signupChangeRequest.setReviewRemark(request.getReviewRemark());
        signupChangeRequest.setUpdatedAt(Instant.now().getEpochSecond());
        signupChangeRequestMapper.updateById(signupChangeRequest);
        return Response.success();
    }
    @Override
    public Response getTeamByCoachId(String coachId, int pageNow, int pageSize, String season) {
        LambdaQueryWrapper<Team> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Team::getCoachId,coachId);
        List<Team> teams = teamMapper.selectList(lambdaQueryWrapper);
        List<String> teamIds = teams.stream().map(Team::getId).toList();
        LambdaQueryWrapper<TeamMember> teamMemberLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamMemberLambdaQueryWrapper.in(TeamMember::getTeamId,teamIds);
        List<TeamMember> teamMembers = teamMemberMapper.selectList(teamMemberLambdaQueryWrapper);
        List<String> memberIds = teamMembers.stream().map(TeamMember::getMemberId).toList();
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.in(User::getId,memberIds);
        List<User> users = userMapper.selectList(userLambdaQueryWrapper);
        List<CoachGetAllTeamMemberResponse.Member> members = new ArrayList<>();
        for(User user : users) {
            CoachGetAllTeamMemberResponse.Member member = new CoachGetAllTeamMemberResponse.Member();
            member.setName(user.getFirstName() + user.getLastName());
            member.setId(user.getId());
            members.add(member);
        }
        CoachGetAllTeamMemberResponse ans = new CoachGetAllTeamMemberResponse();
        ans.setMembers(members);
        return Response.of(ans);
    }



    @Override
    public Response updateTeamNameByCoachId(SignupUpdateTeamNameRequest signupUpdateTeamNameRequest) {
        Signup signup = signupMapper.selectById(signupUpdateTeamNameRequest.getSignupId());
        if (signup == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST);
        }
        Team team = teamMapper.selectById(signup.getTeamId());
        if (team == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST);
        }
        if (!signupUpdateTeamNameRequest.getCoachId().equals(signup.getCoachId())) {
            return Response.error(ErrorCode.ACCESS_DENIED);
        }
        // 先查询是否已存在未处理的修改请求
        LambdaQueryWrapper<SignupChangeRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SignupChangeRequest::getSignupId, signupUpdateTeamNameRequest.getSignupId())
                .eq(SignupChangeRequest::getRequestStatus, 1);
        SignupChangeRequest existingRequest = signupChangeRequestMapper.selectOne(queryWrapper);


        long currentTimestamp = TimeUtil.getCurrentTimestamp();

        if (existingRequest == null) {
            // 如果不存在未处理的请求，则创建新的请求
            SignupChangeRequest newRequest = new SignupChangeRequest();
            newRequest.setId(UUID.randomUUID().toString());
            newRequest.setSignupId(signupUpdateTeamNameRequest.getSignupId());
            newRequest.setTeamNameBefore(signup.getTeamName());
            newRequest.setTeamNameAfter(signupUpdateTeamNameRequest.getTeamNameAfter());
            newRequest.setTeamNameEnBefore(signup.getTeamNameEn());
            newRequest.setTeamNameEnAfter(signupUpdateTeamNameRequest.getTeamNameEnAfter());
            newRequest.setRequestStatus(1); // 待审核状态
            newRequest.setCreatedAt(currentTimestamp);
            newRequest.setUpdatedAt(currentTimestamp);

            int result = signupChangeRequestMapper.insert(newRequest);
            if (result == 1) {
                return Response.success();
            } else {
                return Response.error(ErrorCode.INSERT_FAILED);
            }
        } else {
            // 如果存在未处理的请求，则更新现有请求
            LambdaUpdateWrapper<SignupChangeRequest> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SignupChangeRequest::getId, existingRequest.getId())
                    .set(SignupChangeRequest::getTeamNameBefore, signup.getTeamName())
                    .set(SignupChangeRequest::getTeamNameAfter, signupUpdateTeamNameRequest.getTeamNameAfter())
                    .set(SignupChangeRequest::getTeamNameEnBefore, signup.getTeamNameEn())
                    .set(SignupChangeRequest::getTeamNameEnAfter, signupUpdateTeamNameRequest.getTeamNameEnAfter())
                    .set(SignupChangeRequest::getUpdatedAt, currentTimestamp);

            int result = signupChangeRequestMapper.update(existingRequest, updateWrapper);
            if (result == 1) {
                return Response.success();
            } else {
                return Response.error(ErrorCode.UPDATE_FAILED);
            }
        }
    }

    @Override
    public Response getSignupInfoById(String signupId) {
        // 1. 根据signupId查询报名信息
        Signup signup = signupMapper.selectById(signupId);
        if (signup == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "报名信息不存在");
        }

        SignupInfoResponse response = new SignupInfoResponse();

        // 2. 查询并设置比赛名称
        String competitionId = signup.getCompetitionId();
        if (competitionId != null && !competitionId.isEmpty()) {
            Competition competition = competitionMapper.selectById(competitionId);
            if (competition != null) {
                response.setCompetitionName(competition.getName());
            }
        }

        // 3. 设置队伍信息
        response.setTeamName(signup.getTeamName());
        response.setTeamNameEn(signup.getTeamNameEn());

        // 4. 设置队长ID
        String leaderId = signup.getLeaderId();
        response.setLeaderId(leaderId);

        // 5. 通过signupId查询所有队员信息
        LambdaQueryWrapper<SignupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SignupMember::getSignupId, signupId);
        List<SignupMember> signupMembers = signupMemberMapper.selectList(queryWrapper);

        // 用于存储非队长的队员
        List<String> nonLeaderMemberIds = new ArrayList<>();

        // 6. 遍历所有队员，找出队长和其他队员
        for (SignupMember signupMember : signupMembers) {
            String memberId = signupMember.getMemberId();
            if (memberId.equals(leaderId)) {
                // 队长信息
                User leaderUser = userMapper.selectById(memberId);
                if (leaderUser != null) {
                    response.setLeaderName(leaderUser.getFirstName() + leaderUser.getLastName());
                    response.setLeaderNameEn(leaderUser.getFirstNameEn() + " " + leaderUser.getLastNameEn());
                }
            } else {
                // 非队长队员
                nonLeaderMemberIds.add(memberId);
            }
        }

        // 7. 设置其他队员信息
        if (!nonLeaderMemberIds.isEmpty() && nonLeaderMemberIds.size() >= 1) {
            String member1Id = nonLeaderMemberIds.get(0);
            response.setMember1Id(member1Id);
            User member1User = userMapper.selectById(member1Id);
            if (member1User != null) {
                response.setMember1Name(member1User.getFirstName() + member1User.getLastName());
                response.setMember1NameEn(member1User.getFirstNameEn() + " " + member1User.getLastNameEn());
            }

            if (nonLeaderMemberIds.size() >= 2) {
                String member2Id = nonLeaderMemberIds.get(1);
                response.setMember2Id(member2Id);
                User member2User = userMapper.selectById(member2Id);
                if (member2User != null) {
                    response.setMember2Name(member2User.getFirstName() + member2User.getLastName());
                    response.setMember2NameEn(member2User.getFirstNameEn() + " " + member2User.getLastNameEn());
                }
            }
        }

        // 8. 设置证书教练信息
        String prizeCoachId = signup.getPrizeCoachId();
        if (prizeCoachId != null && !prizeCoachId.isEmpty()) {
            response.setPrizeCoachId(prizeCoachId);
            LambdaQueryWrapper<User> coachQueryWrapper = new LambdaQueryWrapper<>();
            coachQueryWrapper.eq(User::getId, prizeCoachId);
            User prizeCoachUser = userMapper.selectOne(coachQueryWrapper);

            if (prizeCoachUser != null) {
                response.setPrizeCoachName(prizeCoachUser.getFirstName() + prizeCoachUser.getLastName());
                response.setPrizeCoachNameEn(prizeCoachUser.getFirstNameEn() + " " + prizeCoachUser.getLastNameEn());
            }
        }

        // 9. 设置其他字段
        response.setOccupationQuotaType(signup.getOccupationQuotaType());
        if (signup.getOccupationQuotaType() != null) {
            response.setOccupationQuotaTypeName(OccupationQuotaTypeConstant.getTypeById(signup.getOccupationQuotaType().toString()));
        }
        response.setIsFemaleTeam(signup.getIsFemaleTeam());

        // 10. 设置发票信息
        response.setInstitution(signup.getInstitution());
        response.setTaxpayerCode(signup.getTaxpayerCode());
        response.setInvoiceAddress(signup.getInvoiceAddress());
        response.setInvoicePhone(signup.getInvoicePhone());
        response.setBankName(signup.getBankName());
        response.setBankCardCode(signup.getBankCardCode());

        return Response.of(response);
    }

    @Override
    public Response checkSignupChanging(String signupId) {
        if (signupId == null || signupId.isEmpty()) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "报名ID不能为空");
        }

        // 查询signup_change_request表中状态为请求中的记录
        LambdaQueryWrapper<SignupChangeRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SignupChangeRequest::getSignupId, signupId)
                    .eq(SignupChangeRequest::getRequestStatus, 1);

        // 查询记录数
        Long count = signupChangeRequestMapper.selectCount(queryWrapper);

        // 如果存在记录，返回true，否则返回false
        boolean hasChangingRequest = count > 0;

        return Response.of(hasChangingRequest);
    }

    @Override
    public Response getSchoolCoach() {
        // 1. 获取当前登录用户ID
        String currentUserId = RoleUtil.getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            throw new BizException(ErrorCode.NO_SIGN_IN.getErrCode(), "用户未登录");
        }

        // 2. 获取当前用户
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "用户不存在");
        }

        // 3. 获取当前用户的学校ID
        String schoolId = currentUser.getSchoolId();
        if (schoolId == null || schoolId.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "用户未关联学校");
        }

        // 4. 获取同一学校的所有教练
        List<CoachInfoResponse> coaches = signupMapper.getSchoolCoaches(schoolId);

        // 5. 如果没有找到教练，返回空列表
        if (coaches == null) {
            return Response.of(new ArrayList<>());
        }

        return Response.of(coaches);
    }

    @Override
    public Response getCoachStudent() {
        // 1. 获取当前登录用户ID
        String currentUserId = RoleUtil.getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            throw new BizException(ErrorCode.NO_SIGN_IN.getErrCode(), "用户未登录");
        }

        // 2. 查询教练ID为当前用户ID的学生
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getCoachId, currentUserId)
                   .select(User::getId, User::getFirstName, User::getLastName,
                           User::getFirstNameEn, User::getLastNameEn);

        List<User> students = userMapper.selectList(queryWrapper);

        // 如果没有找到学生，返回空列表
        if (students == null || students.isEmpty()) {
            return Response.of(new ArrayList<>());
        }

        // 3. 转换为前端所需的响应格式
        List<StudentInfoResponse> studentInfoResponses = new ArrayList<>();
        for (User student : students) {
            StudentInfoResponse response = new StudentInfoResponse();
            response.setId(student.getId());
            response.setName(student.getFirstName() + student.getLastName());
            response.setNameEn(student.getFirstNameEn() + " " + student.getLastNameEn());
            studentInfoResponses.add(response);
        }

        return Response.of(studentInfoResponses);
    }

    @Override
    public Response getSignupRequestChangeBySignupId(String signupId) {
        // 验证报名ID是否为空
        if (signupId == null || signupId.isEmpty()) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "报名ID不能为空");
        }

        // 验证报名信息是否存在
        Signup signup = signupMapper.selectById(signupId);
        if (signup == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "报名信息不存在");
        }

        // 查询历史修改申请
        LambdaQueryWrapper<SignupChangeRequest> changeRequestQueryWrapper = new LambdaQueryWrapper<>();
        changeRequestQueryWrapper.eq(SignupChangeRequest::getSignupId, signupId)
                .select(SignupChangeRequest::getId,
                        SignupChangeRequest::getRequestStatus,
                        SignupChangeRequest::getCreatedAt,
                        SignupChangeRequest::getUpdatedAt,
                        SignupChangeRequest::getReviewRemark)
                .orderByDesc(SignupChangeRequest::getCreatedAt);

        List<SignupChangeRequest> changeRequests = signupChangeRequestMapper.selectList(changeRequestQueryWrapper);
        List<SignupChangeRequestItem> changeRequestItems = new ArrayList<>();

        if (changeRequests != null && !changeRequests.isEmpty()) {
            for (SignupChangeRequest request : changeRequests) {
                SignupChangeRequestItem item = new SignupChangeRequestItem();
                item.setId(request.getId());
                item.setRequestStatus(request.getRequestStatus());
                item.setCreatedAt(request.getCreatedAt());
                item.setUpdatedAt(request.getUpdatedAt());
                item.setReviewRemark(request.getReviewRemark());

                changeRequestItems.add(item);
            }
        }

        return Response.of(changeRequestItems);
    }

    @Override
    public Response getSchoolCompetitionQuotaRest(String competitionId) {
        // 1. 验证比赛ID是否为空
        if (competitionId == null || competitionId.isEmpty()) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "比赛ID不能为空");
        }

        // 2. 获取当前登录用户ID
        String currentUserId = RoleUtil.getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            throw new BizException(ErrorCode.NO_SIGN_IN.getErrCode(), "用户未登录");
        }

        // 3. 查询当前用户的学校ID
        User currentUser = userMapper.selectById(currentUserId);
        String schoolId = currentUser.getSchoolId();
        if (schoolId == null || schoolId.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "用户未关联学校");
        }

        // 4. 查询学校在该比赛中的配额余量
        LambdaQueryWrapper<QuotaDistribution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuotaDistribution::getCompetitionId, competitionId)
                   .eq(QuotaDistribution::getSchoolId, schoolId);

        QuotaDistribution quotaDistribution = quotaDistributionMapper.selectOne(queryWrapper);

        // 5. 构建响应对象
        SchoolCompetitionQuotaRestResponse response = new SchoolCompetitionQuotaRestResponse();
        response.setQuotaSimpleRest(quotaDistribution.getQuotaSimpleRest());
        response.setQuotaGirlRest(quotaDistribution.getQuotaGirlRest());
        response.setQuotaAdditionRest(quotaDistribution.getQuotaAdditionRest());
        response.setQuotaUnofficialRest(quotaDistribution.getQuotaUnofficialRest());
        return Response.of(response);
    }

    @Override
    public Response getSignupByCoachId(int pageNow,int pageSize, String competition, String teamMembers, String teamName, Integer processStatus, Integer occupationQuotaType,String prizeCoach) {
        String[] members = null;
        if(teamMembers!=null) members = teamMembers.split(" ");
        List<Signup> signups = null;
        if (members != null) {
            signups = signupMapper.getSignupByCoachId(RoleUtil.getCurrentUserId(),(pageNow-1)* pageSize,pageSize,prizeCoach,competition,teamName,processStatus,occupationQuotaType, Arrays.asList(members));
        }
        Integer totalCount = null;
        if (members != null) {
            totalCount = signupMapper.countSignupByCoachId(competition, teamName, processStatus, occupationQuotaType, prizeCoach, Arrays.asList(members) , RoleUtil.getCurrentUserId());
        }
        CoachGetAllSignupResponse coachGetAllSignupResponse = new CoachGetAllSignupResponse();
        List<CoachGetAllSignupResponse.Signups> signupsRes = new ArrayList<>();
        if (signups != null) {
            for(Signup signup:signups)
            {
                CoachGetAllSignupResponse.Signups signupsResponse = new CoachGetAllSignupResponse.Signups();
                BeanUtils.copyProperties(signup,signupsResponse);
                signupsResponse.setSignupId(signup.getId());
                signupsResponse.setLeaderName(getUserById(signup.getLeaderId()));
                // 获取队伍成员
                LambdaQueryWrapper<SignupMember> queryWrapper3 = new LambdaQueryWrapper<>();
                queryWrapper3.eq(SignupMember::getSignupId,signup.getId());
                List<SignupMember> signupMembers = signupMemberMapper.selectList(queryWrapper3);
                List<String> teamMember = new ArrayList<>();
                List<String> teamMemberEn = new ArrayList<>();
                for(SignupMember signupMember : signupMembers)
                {
                    String memberId = signupMember.getMemberId();
                    teamMember.add(getUserById(memberId));
                    User user = userMapper.selectById(memberId);
                    String memberNameEn =user.getFirstNameEn()+user.getLastNameEn();
                    teamMemberEn.add(memberNameEn);
                }
                signupsResponse.setTeamMembersName(teamMember);
                signupsResponse.setTeamMembersNameEn(teamMemberEn);
                signupsResponse.setCompetition(competitionMapper.selectById(signup.getCompetitionId()).getName());
                signupsResponse.setPrizeCoach(getUserById(signup.getPrizeCoachId()));
                signupsResponse.setProcessStatus(signup.getProcessStatus());
                signupsResponse.setOccupationQuotaType(signup.getOccupationQuotaType());
                signupsResponse.setSchool(schoolMapper.selectById(signup.getSchoolId()).getName());
                signupsResponse.setCoach(getUserById(signup.getCoachId()));
                signupsRes.add(signupsResponse);
            }
        }
        coachGetAllSignupResponse.setSignups(signupsRes);
        coachGetAllSignupResponse.setTotal(totalCount);
        return Response.of(coachGetAllSignupResponse);
    }

    @Override
    public Response getAllSchoolName() {
        List<School> schools = schoolMapper.selectList(null);
        AdminGetAllSchoolNameResponse adminGetAllSchoolNameResponse = new AdminGetAllSchoolNameResponse();
        List<AdminGetAllSchoolNameResponse.School> schoolList = new ArrayList<>();
        for(School school:schools)
        {
            AdminGetAllSchoolNameResponse.School school1 = new AdminGetAllSchoolNameResponse.School();
            school1.setId(school.getId());
            school1.setName(school.getName());
            schoolList.add(school1);
        }
        adminGetAllSchoolNameResponse.setSchoolList(schoolList);
        return Response.of(adminGetAllSchoolNameResponse);
    }

    @Override
    public Response uploadPayment(MultipartFile file,String SignupId) {
        // todo upload
        Signup signup = signupMapper.selectById(SignupId);
        signup.setProcessStatus(SignupStatusConstant.PAYMENT_UPLOADED);
        signupMapper.updateById(signup);

        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response signupCompetitions(String competitionId, List<SignupCompetitionRequest> requests) {
        // 1. 验证比赛是否存在
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }

        // 2. 验证当前时间是否在报名时间范围内
        long currentTime = TimeUtil.getCurrentTimestamp();
        if (currentTime < competition.getRegistrationStartTime() || currentTime > competition.getRegistrationEndTime()) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "当前不在比赛报名时间范围内");
        }

        // 3. 遍历处理所有请求
        for (SignupCompetitionRequest request : requests) {
            try {
                // 3.1 检查队员ID是否重复
                Set<String> memberIds = new HashSet<>();

                // 检查队长ID
                if (request.getLeaderId() != null && !request.getLeaderId().isEmpty()) {
                    memberIds.add(request.getLeaderId());
                }

                // 检查队员1 ID
                if (request.getMember1Id() != null && !request.getMember1Id().isEmpty()) {
                    if (!memberIds.add(request.getMember1Id())) {
                        return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(),
                                "参赛成员重复");
                    }
                }

                // 检查队员2 ID
                if (request.getMember2Id() != null && !request.getMember2Id().isEmpty()) {
                    if (!memberIds.add(request.getMember2Id())) {
                        return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(),
                                "参赛成员重复");
                    }
                }

                // 3.2 检查女队名额
                boolean allFemale = checkAllMembersFemale(
                        request.getLeaderId(),
                        request.getMember1Id(),
                        request.getMember2Id());
                if (request.getOccupationQuotaType() != null &&
                        OccupationQuotaTypeConstant.FEMALE.equals(request.getOccupationQuotaType())) {
                    // 如果使用了女队名额，检查所有队员是否为女性
                    if (!allFemale) {
                        return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(),
                                "使用女队名额的队伍必须全部由女性组成");
                    }
                }

                // 3.3 如果设置了isFemaleTeam=true，检查所有队员是否为女性
                if (request.getIsFemaleTeam() != null && request.getIsFemaleTeam()) {
                    if (!allFemale) {
                        return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(),
                                "女队的队员必须全部为女性");
                    }
                }

                // 4. 创建报名记录
                Signup signup = new Signup();
                BeanUtils.copyProperties(request, signup);

                // 设置比赛ID
                signup.setCompetitionId(competitionId);

                // 设置ID和时间戳
                String signupId = UUID.randomUUID().toString();
                signup.setId(signupId);
                signup.setCreatedAt(currentTime);
                signup.setUpdatedAt(currentTime);

                // 设置初始流程状态为已报名
                signup.setProcessStatus(SignupStatusConstant.SIGNED_UP);

                // 插入报名记录
                signupMapper.insert(signup);

                // 创建并插入队员记录
                // 队长
                SignupMember leaderMember = new SignupMember();
                leaderMember.setId(UUID.randomUUID().toString());
                leaderMember.setSignupId(signupId);
                leaderMember.setMemberId(request.getLeaderId());
                signupMemberMapper.insert(leaderMember);

                // 队员1
                if (request.getMember1Id() != null && !request.getMember1Id().isEmpty()) {
                    SignupMember member1 = new SignupMember();
                    member1.setId(UUID.randomUUID().toString());
                    member1.setSignupId(signupId);
                    member1.setMemberId(request.getMember1Id());
                    signupMemberMapper.insert(member1);
                }

                // 队员2
                if (request.getMember2Id() != null && !request.getMember2Id().isEmpty()) {
                    SignupMember member2 = new SignupMember();
                    member2.setId(UUID.randomUUID().toString());
                    member2.setSignupId(signupId);
                    member2.setMemberId(request.getMember2Id());
                    signupMemberMapper.insert(member2);
                }

            } catch (Exception e) {
                throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "插入报名信息失败: " + e.getMessage());
            }
        }

        return Response.success();
    }

    @Override
    public Response updateSignupInfo(SignupUpdateInfoRequest request) {
        // 1. 验证报名ID是否为空
        if (request.getSignupId() == null || request.getSignupId().isEmpty()) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "报名ID不能为空");
        }

        // 2. 查询报名信息是否存在
        Signup signup = signupMapper.selectById(request.getSignupId());
        if (signup == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "报名信息不存在");
        }

        // 3. 检查队员是否有重复
        if (request.getNewLeaderId() != null && request.getNewMember1Id() != null
                && request.getNewLeaderId().equals(request.getNewMember1Id())) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "队长和队员1不能是同一人");
        }

        if (request.getNewLeaderId() != null && request.getNewMember2Id() != null
                && request.getNewLeaderId().equals(request.getNewMember2Id())) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "队长和队员2不能是同一人");
        }

        if (request.getNewMember1Id() != null && request.getNewMember2Id() != null
                && request.getNewMember1Id().equals(request.getNewMember2Id())) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "队员1和队员2不能是同一人");
        }

        // 4. 检查新旧占用名额类型是否发生变化，如果变化则检查新名额类型是否有余量
        if (request.getNewOccupationQuotaType() != null && signup.getOccupationQuotaType() != null
                && !request.getNewOccupationQuotaType().equals(signup.getOccupationQuotaType())) {

            String competitionId = signup.getCompetitionId();

            // 调用getSchoolCompetitionQuotaRest方法获取学校名额余量
            Response quotaResponse = getSchoolCompetitionQuotaRest(competitionId);

            SchoolCompetitionQuotaRestResponse quotaRest = (SchoolCompetitionQuotaRestResponse) quotaResponse.getData();

            // 根据新的占用名额类型检查是否有余量
            boolean hasQuotaRest = false;
            String quotaTypeMsg = "";
            Integer quotaType = request.getNewOccupationQuotaType();

            if (OccupationQuotaTypeConstant.NORMAL.equals(quotaType)) {
                hasQuotaRest = quotaRest.getQuotaSimpleRest() != null && quotaRest.getQuotaSimpleRest() >= 1;
                quotaTypeMsg = "普通名额";
            } else if (OccupationQuotaTypeConstant.FEMALE.equals(quotaType)) {
                hasQuotaRest = quotaRest.getQuotaGirlRest() != null && quotaRest.getQuotaGirlRest() >= 1;
                quotaTypeMsg = "女队名额";
            } else if (OccupationQuotaTypeConstant.WILDCARD.equals(quotaType)) {
                hasQuotaRest = quotaRest.getQuotaAdditionRest() != null && quotaRest.getQuotaAdditionRest() >= 1;
                quotaTypeMsg = "外卡名额";
            } else if (OccupationQuotaTypeConstant.STAR.equals(quotaType)) {
                hasQuotaRest = quotaRest.getQuotaUnofficialRest() != null && quotaRest.getQuotaUnofficialRest() >= 1;
                quotaTypeMsg = "打星名额";
            } else {
                throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "无效的占用名额类型");
            }

            if (!hasQuotaRest) {
                throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "学校在该比赛下的" + quotaTypeMsg + "已用完，无法更换名额类型");
            }
        }

        // 5. 检查女队名额要求
        boolean allFemale = checkAllMembersFemale(request.getNewLeaderId(), request.getNewMember1Id(), request.getNewMember2Id());
        if (request.getIsFemaleTeam() != null && request.getIsFemaleTeam()) {
            if (!allFemale) {
                throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "女队的队员必须全部为女性");
            }
        }

        if (request.getNewOccupationQuotaType() != null && request.getNewOccupationQuotaType().equals(OccupationQuotaTypeConstant.FEMALE)) {
            if (!allFemale) {
                throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "使用女队名额的队伍必须全部由女性组成");
            }
        }

        SignupUpdateInfoResponse response = new SignupUpdateInfoResponse();

        // 6. 根据processStatus决定处理逻辑
        if (signup.getProcessStatus() != null && signup.getProcessStatus() == SignupStatusConstant.SIGNED_UP) {
            // 已报名状态未审核：直接修改
            directUpdate(signup, request);
            response.setDirectUpdate(true);
            response.setMessage("报名信息已成功更新");
        } else {
            // 其他状态：提交修改申请
            String requestId = createNewChangeRequest(signup, request);
            response.setDirectUpdate(false);
            response.setRequestId(requestId);
            response.setMessage("报名信息修改申请已提交，等待审核");
        }

        return Response.of(response);
    }

    /**
     * 直接更新报名信息
     */
    private void directUpdate(Signup signup, SignupUpdateInfoRequest request) {
        // 更新报名信息
        if (request.getNewTeamName() != null) {
            signup.setTeamName(request.getNewTeamName());
        }

        if (request.getNewTeamNameEn() != null) {
            signup.setTeamNameEn(request.getNewTeamNameEn());
        }

        if (request.getNewLeaderId() != null) {
            signup.setLeaderId(request.getNewLeaderId());
        }

        if (request.getNewPrizeCoachId() != null) {
            signup.setPrizeCoachId(request.getNewPrizeCoachId());
        }

        if (request.getNewOccupationQuotaType() != null) {
            // 检查新旧名额类型是否有变化
            Integer oldType = signup.getOccupationQuotaType();
            Integer newType = request.getNewOccupationQuotaType();

            if (!oldType.equals(newType)) {
                // 名额类型发生变化，需要更新quota_distribution表
                LambdaQueryWrapper<QuotaDistribution> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(QuotaDistribution::getCompetitionId, signup.getCompetitionId())
                        .eq(QuotaDistribution::getSchoolId, signup.getSchoolId());

                QuotaDistribution quotaDistribution = quotaDistributionMapper.selectOne(queryWrapper);

                if (quotaDistribution != null) {
                    // 释放旧名额（对应字段+1）
                    if (OccupationQuotaTypeConstant.NORMAL.equals(oldType)) {
                        Long oldRest = quotaDistribution.getQuotaSimpleRest();
                        quotaDistribution.setQuotaSimpleRest(oldRest + 1);
                    } else if (OccupationQuotaTypeConstant.FEMALE.equals(oldType)) {
                        Long oldRest = quotaDistribution.getQuotaGirlRest();
                        quotaDistribution.setQuotaGirlRest(oldRest + 1);
                    } else if (OccupationQuotaTypeConstant.WILDCARD.equals(oldType)) {
                        Long oldRest = quotaDistribution.getQuotaAdditionRest();
                        quotaDistribution.setQuotaAdditionRest(oldRest + 1);
                    } else if (OccupationQuotaTypeConstant.STAR.equals(oldType)) {
                        Long oldRest = quotaDistribution.getQuotaUnofficialRest();
                        quotaDistribution.setQuotaUnofficialRest(oldRest + 1);
                    }

                    // 占用新名额（对应字段-1）
                    if (OccupationQuotaTypeConstant.NORMAL.equals(newType)) {
                        Long newRest = quotaDistribution.getQuotaSimpleRest();
                        quotaDistribution.setQuotaSimpleRest(newRest - 1);
                    } else if (OccupationQuotaTypeConstant.FEMALE.equals(newType)) {
                        Long newRest = quotaDistribution.getQuotaGirlRest();
                        quotaDistribution.setQuotaGirlRest(newRest - 1);
                    } else if (OccupationQuotaTypeConstant.WILDCARD.equals(newType)) {
                        Long newRest = quotaDistribution.getQuotaAdditionRest();
                        quotaDistribution.setQuotaAdditionRest(newRest - 1);
                    } else if (OccupationQuotaTypeConstant.STAR.equals(newType)) {
                        Long newRest = quotaDistribution.getQuotaUnofficialRest();
                        quotaDistribution.setQuotaUnofficialRest(newRest - 1);
                    }

                    // 更新数据库
                    quotaDistributionMapper.updateById(quotaDistribution);
                }
            }

            // 更新报名表中的名额类型
            signup.setOccupationQuotaType(newType);
        }

        if (request.getIsFemaleTeam() != null) {
            signup.setIsFemaleTeam(request.getIsFemaleTeam());
        }

        if (request.getInstitution() != null) {
            signup.setInstitution(request.getInstitution());
        }

        if (request.getTaxpayerCode() != null) {
            signup.setTaxpayerCode(request.getTaxpayerCode());
        }

        if (request.getInvoiceAddress() != null) {
            signup.setInvoiceAddress(request.getInvoiceAddress());
        }

        if (request.getInvoicePhone() != null) {
            signup.setInvoicePhone(request.getInvoicePhone());
        }

        if (request.getBankName() != null) {
            signup.setBankName(request.getBankName());
        }

        if (request.getBankCardCode() != null) {
            signup.setBankCardCode(request.getBankCardCode());
        }

        // 更新修改时间
        signup.setUpdatedAt(TimeUtil.getCurrentTimestamp());

        // 保存更新后的报名信息
        signupMapper.updateById(signup);

        // 更新队员信息
        updateTeamMembers(request.getSignupId(), request.getOldMember1Id(), request.getNewMember1Id());
        updateTeamMembers(request.getSignupId(), request.getOldMember2Id(), request.getNewMember2Id());
    }

    /**
     * 创建新的变更申请
     */
    private String createNewChangeRequest(Signup signup, SignupUpdateInfoRequest request) {
        long currentTimestamp = TimeUtil.getCurrentTimestamp();
        SignupChangeRequest newRequest = new SignupChangeRequest();

        // 设置基本信息
        String requestId = UUID.randomUUID().toString();
        newRequest.setId(requestId);
        newRequest.setSignupId(request.getSignupId());
        newRequest.setRequestStatus(1); // 请求中状态
        newRequest.setCreatedAt(currentTimestamp);
        newRequest.setUpdatedAt(currentTimestamp);

        // 设置队伍名称相关字段
        newRequest.setTeamNameBefore(signup.getTeamName());
        newRequest.setTeamNameAfter(request.getNewTeamName());
        newRequest.setTeamNameEnBefore(signup.getTeamNameEn());
        newRequest.setTeamNameEnAfter(request.getNewTeamNameEn());

        // 设置队长ID
        newRequest.setLeaderIdBefore(signup.getLeaderId());
        newRequest.setLeaderIdAfter(request.getNewLeaderId());

        //设置队员ID
        newRequest.setMember1IdBefore(request.getOldMember1Id());
        newRequest.setMember1IdAfter(request.getNewMember1Id());
        newRequest.setMember2IdBefore(request.getOldMember2Id());
        newRequest.setMember2IdAfter(request.getNewMember2Id());

        // 设置证书教练ID
        newRequest.setPrizeCoachIdBefore(signup.getPrizeCoachId());
        newRequest.setPrizeCoachIdAfter(request.getNewPrizeCoachId());

        // 设置占用名额类型
        newRequest.setOccupationQuotaTypeBefore(signup.getOccupationQuotaType());
        newRequest.setOccupationQuotaTypeAfter(request.getNewOccupationQuotaType());

        // 设置是否女队
        newRequest.setIsFemaleTeamBefore(signup.getIsFemaleTeam());
        newRequest.setIsFemaleTeamAfter(request.getIsFemaleTeam());

        // 设置发票信息
        newRequest.setInstitutionBefore(signup.getInstitution());
        newRequest.setInstitutionAfter(request.getInstitution());

        newRequest.setTaxpayerCodeBefore(signup.getTaxpayerCode());
        newRequest.setTaxpayerCodeAfter(request.getTaxpayerCode());

        newRequest.setInvoiceAddressBefore(signup.getInvoiceAddress());
        newRequest.setInvoiceAddressAfter(request.getInvoiceAddress());

        newRequest.setInvoicePhoneBefore(signup.getInvoicePhone());
        newRequest.setInvoicePhoneAfter(request.getInvoicePhone());

        newRequest.setBankNameBefore(signup.getBankName());
        newRequest.setBankNameAfter(request.getBankName());

        newRequest.setBankCardCodeBefore(signup.getBankCardCode());
        newRequest.setBankCardCodeAfter(request.getBankCardCode());

        // 插入新申请
        signupChangeRequestMapper.insert(newRequest);

        return requestId;
    }

    /**
     * 检查所有队员是否都是女性
     */
    private boolean checkAllMembersFemale(String leaderId, String member1Id, String member2Id) {
        if (leaderId!= null &&!isFemaleMember(leaderId)) {
            return false;
        }
        if (member1Id!= null &&!isFemaleMember(member1Id)) {
            return false;
        }
        if (member2Id!= null &&!isFemaleMember(member2Id)){
            return false;
        }
        return true;
    }

    /**
     * 检查用户是否为女性
     */
    private boolean isFemaleMember(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        return user.getSex();
    }

    /**
     * 更新队员信息
     */
    private void updateTeamMembers(String signupId, String oldMemberId, String newMemberId) {
        if (oldMemberId == null && newMemberId == null) {
            return;
        }
        // 如果oldMemberId为空，则新插入一条signUpMember记录
        if (oldMemberId == null && newMemberId != null) {
            SignupMember newSignupMember = new SignupMember();
            newSignupMember.setId(UUID.randomUUID().toString());
            newSignupMember.setSignupId(signupId);
            newSignupMember.setMemberId(newMemberId);
            signupMemberMapper.insert(newSignupMember);
            return;
        }
        // 如果newMemberId为空，则删除原有的signUpMember记录
        if (newMemberId == null) {
            LambdaQueryWrapper<SignupMember> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SignupMember::getSignupId, signupId)
                    .eq(SignupMember::getMemberId, oldMemberId);
            signupMemberMapper.delete(queryWrapper);
            return;
        }
        // 如果新旧ID相同，不需要更新
        if (oldMemberId.equals(newMemberId)) {
            return;
        }
        LambdaQueryWrapper<SignupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SignupMember::getSignupId, signupId)
                .eq(SignupMember::getMemberId, oldMemberId);

        SignupMember signupMember = signupMemberMapper.selectOne(queryWrapper);
        signupMember.setMemberId(newMemberId);
        signupMemberMapper.updateById(signupMember);
    }

}
