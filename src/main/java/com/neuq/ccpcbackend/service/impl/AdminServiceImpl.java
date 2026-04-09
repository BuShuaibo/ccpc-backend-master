package com.neuq.ccpcbackend.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.entity.*;
import com.neuq.ccpcbackend.mapper.*;
import com.neuq.ccpcbackend.service.AdminService;
import com.neuq.ccpcbackend.utils.ExcelUtil;
import com.neuq.ccpcbackend.utils.RoleUtil;
import com.neuq.ccpcbackend.utils.TimeUtil;
import com.neuq.ccpcbackend.utils.exception.BizException;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.response.Response;
import com.neuq.ccpcbackend.vo.AdminGetAllAnnouncementVo;
import com.neuq.ccpcbackend.vo.SchoolGetAllCoachVo;
import com.neuq.ccpcbackend.vo.SchoolGetAllCompetitionAdminVo;
import com.neuq.ccpcbackend.vo.UserGetCoachAndSchoolVo;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    @Resource
    UserMapper userMapper;

    @Resource
    RoleMapper roleMapper;

    @Resource
    UserRoleMapper userRoleMapper;

    @Resource
    CompetitionMapper competitionMapper;

    @Resource
    private SchoolMapper schoolMapper;

    @Resource
    private CompetitionTypeMapper competitionTypeMapper;

    @Resource
    private SeasonMapper seasonMapper;

    @Resource
    private SystemInfoMapper systemInfoMapper;

    @Resource
    private SlideshowMapper slideshowMapper;
    
    @Resource
    private CompetitionGroupCategoryMapper competitionGroupCategoryMapper;

    @Resource
    private AnnouncementMapper announcementMapper;

    @Resource
    private SponsorMapper sponsorMapper;

    @Override
    public Response getAllRole() {
        List<Role> roles = roleMapper.selectList(null);
        return Response.of(roles);
    }

    @Override
    public Response getUserCount(AdminGetUserCountRequest adminGetUserCountRequest) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 学校查询
        String keywordSchool = adminGetUserCountRequest.getKeywordSchool();
        if (keywordSchool != null && !keywordSchool.isEmpty()) {
            LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
            queryWrapperSchool.and(qw -> qw.like(School::getName, keywordSchool));
            queryWrapperSchool.select(School::getId, School::getName);
            List<School> schools = schoolMapper.selectList(queryWrapperSchool);
            List<String> schoolIds = schools.stream().map(School::getId).toList();
            if (!schoolIds.isEmpty()) {
                queryWrapper.and(qw -> qw.in(User::getSchoolId, schoolIds));
            } else {
                return Response.of(0);
            }
        }
        // 身份查询
        List<String> keywordRoleIds = adminGetUserCountRequest.getKeywordRoleIds();
        if (keywordRoleIds != null && !keywordRoleIds.isEmpty()) {
            String roleIdList = keywordRoleIds.stream()
                    .map(id -> "'" + id + "'")
                    .collect(Collectors.joining(","));
            queryWrapper.and(qw -> qw.apply(
                    "EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = user.id AND ur.role_id IN (" + roleIdList + "))"
            ));
        }
        // 名字查询
        String keywordName = adminGetUserCountRequest.getKeywordName();
        if (keywordName != null && !keywordName.isEmpty()) {
            queryWrapper.apply("CONCAT(first_name, last_name) LIKE {0}",
                            "%" + keywordName + "%");
        }
        // 英文名字查询
        String keywordNameEn = adminGetUserCountRequest.getKeywordNameEn();
        if (keywordNameEn != null && !keywordNameEn.isEmpty()) {
            queryWrapper.apply("CONCAT(first_name_en, last_name_en) LIKE {0}",
                    "%" + keywordNameEn + "%");
        }
        // 手机号查询
        String keywordPhone = adminGetUserCountRequest.getKeywordPhone();
        if (keywordPhone != null && !keywordPhone.isEmpty()) {
            queryWrapper.and(qw -> qw.like(User::getPhone, keywordPhone));
        }
        // 邮箱查询
        String keywordEmail = adminGetUserCountRequest.getKeywordEmail();
        if (keywordEmail != null && !keywordEmail.isEmpty()) {
            queryWrapper.and(qw -> qw.like(User::getEmail, keywordEmail));
        }
        Long count = userMapper.selectCount(queryWrapper);
        return Response.of(count);
    }

    @Override
    public Response getAllUser(AdminGetAllUserRequest adminGetAllUserRequest) {

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(
                User::getId, User::getFirstName, User::getLastName, User::getFirstNameEn,
                User::getLastNameEn, User::getPhone, User::getEmail, User::getSex, User::getSchoolId);
        // 学校查询
        String keywordSchool = adminGetAllUserRequest.getKeywordSchool();
        if (keywordSchool != null && !keywordSchool.isEmpty()) {
            LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
            queryWrapperSchool.and(qw -> qw.like(School::getName, keywordSchool));
            queryWrapperSchool.select(School::getId, School::getName);
            List<School> schools = schoolMapper.selectList(queryWrapperSchool);
            List<String> schoolIds = schools.stream().map(School::getId).toList();
            if (!schoolIds.isEmpty()) {
                queryWrapper.and(qw -> qw.in(User::getSchoolId, schoolIds));
            } else {
                return Response.of(new ArrayList<>());
            }
        }
        // 身份查询
        List<String> keywordRoleIds = adminGetAllUserRequest.getKeywordRoleIds();
        if (keywordRoleIds != null && !keywordRoleIds.isEmpty()) {
            String roleIdList = keywordRoleIds.stream()
                    .map(id -> "'" + id + "'")
                    .collect(Collectors.joining(","));
            queryWrapper.and(qw -> qw.apply(
                    "EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = user.id AND ur.role_id IN (" + roleIdList + "))"
            ));
        }
        // 名字查询
        String keywordName = adminGetAllUserRequest.getKeywordName();
        if (keywordName != null && !keywordName.isEmpty()) {
            queryWrapper.apply("CONCAT(first_name, last_name) LIKE {0}",
                    "%" + keywordName + "%");
        }
        // 英文名字查询
        String keywordNameEn = adminGetAllUserRequest.getKeywordNameEn();
        if (keywordNameEn != null && !keywordNameEn.isEmpty()) {
            queryWrapper.apply("CONCAT(first_name_en, last_name_en) LIKE {0}",
                    "%" + keywordNameEn + "%");
        }
        // 手机号查询
        String keywordPhone = adminGetAllUserRequest.getKeywordPhone();
        if (keywordPhone != null && !keywordPhone.isEmpty()) {
            queryWrapper.and(qw -> qw.like(User::getPhone, keywordPhone));
        }
        // 邮箱查询
        String keywordEmail = adminGetAllUserRequest.getKeywordEmail();
        if (keywordEmail != null && !keywordEmail.isEmpty()) {
            queryWrapper.and(qw -> qw.like(User::getEmail, keywordEmail));
        }

        // 分页查询用户
        int pageNow = adminGetAllUserRequest.getPageNow();
        int pageSize = adminGetAllUserRequest.getPageSize();
        Page<User> page = new Page<>(pageNow, pageSize);
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        List<User> users = userPage.getRecords();

        // 查询每个用户的身份和学校
        List<AdminGetAllUserResponse> adminGetAllUserResponses = new ArrayList<>();
        for (User user : users) {
            AdminGetAllUserResponse adminGetAllUserResponse = new AdminGetAllUserResponse();
            BeanUtils.copyProperties(user, adminGetAllUserResponse);

            // 填入身份
            List<String> roles = userMapper.getAllRoleByUserId(user.getId());
            adminGetAllUserResponse.setRoleIds(roles);
            adminGetAllUserResponses.add(adminGetAllUserResponse);

            // 填入学校名
            if (user.getSchoolId() != null && !user.getSchoolId().isEmpty()) {
                LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
                queryWrapperSchool.eq(School::getId, user.getSchoolId()).select(School::getId, School::getName);
                School school = schoolMapper.selectOne(queryWrapperSchool);
                if (school != null) {
                    adminGetAllUserResponse.setSchool(school.getName());
                }
            }
        }
        return Response.of(adminGetAllUserResponses);
    }

    @Override
    public Response getUserInfoById(String userId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "用户不存在");
        }
        AdminGetUserInfoByIdResponse adminGetUserInfoByIdResponse = new AdminGetUserInfoByIdResponse();
        BeanUtils.copyProperties(user, adminGetUserInfoByIdResponse);
        // 查询身份
        List<String> roles = userMapper.getAllRoleByUserId(userId);
        adminGetUserInfoByIdResponse.setRoleIds(roles);
        // 查询教练和学校
        UserGetCoachAndSchoolVo coachAndSchool = userMapper.getCoachAndSchoolByUserId(userId);
        adminGetUserInfoByIdResponse.setCoach(coachAndSchool.getCoach());
        adminGetUserInfoByIdResponse.setSchool(coachAndSchool.getSchool());
        return Response.of(adminGetUserInfoByIdResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response updateUserInfoById(AdminUpdateUserInfoByIdRequest adminUpdateUserInfoByIdRequest) {
        // 更新信息
        User user = new User();
        BeanUtils.copyProperties(adminUpdateUserInfoByIdRequest, user);
        int i = userMapper.updateById(user);  // 只会更新非null的字段
        if (i != 1) {
            throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "更新信息失败");
        }
        
        // 更新角色
        List<String> oldRoleIds = userMapper.getAllRoleByUserId(user.getId());
        List<String> newRoleIds = adminUpdateUserInfoByIdRequest.getRoleIds();
        Set<String> oldRoleIdsSet = new HashSet<>(oldRoleIds);
        Set<String> newRoleIdsSet = new HashSet<>(newRoleIds);
        // 需要删除的角色 = 现有角色 - 新角色
        Set<String> rolesToRemove = oldRoleIdsSet.stream()
                .filter(id -> !newRoleIdsSet.contains(id))
                .collect(Collectors.toSet());
        // 需要新增的角色 = 新角色 - 现有角色
        Set<String> rolesToAdd = newRoleIds.stream()
                .filter(id -> !oldRoleIdsSet.contains(id))
                .collect(Collectors.toSet());
        // 删除不要的角色
        if (!rolesToRemove.isEmpty()) {
            LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserRole::getUserId, user.getId())
                    .in(UserRole::getRoleId, rolesToRemove);
            int delete = userRoleMapper.delete(queryWrapper);
            if (delete != rolesToRemove.size()) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "删除旧身份失败");
            }
        }
        // 增加新的的角色
        if (!rolesToAdd.isEmpty()) {
            List<UserRole> userRolesToAdd = rolesToAdd.stream()
                    .map(roleId -> new UserRole()
                            .setId(UUID.randomUUID().toString())
                            .setUserId(user.getId())
                            .setRoleId(roleId))
                    .toList();
            // 从输出的sql来看，这个新方法也是个假批量插入
            List<BatchResult> insert = userRoleMapper.insert(userRolesToAdd);
            int totalInserted = insert.stream()
                    .flatMapToInt(result -> result.getUpdateCounts() != null ?
                            java.util.Arrays.stream(result.getUpdateCounts()) : java.util.stream.IntStream.empty())
                    .sum();
            if (totalInserted != userRolesToAdd.size()) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "增加新身份失败");
            }
        }
        return Response.success();
    }

    @Override
    public Response getCompetitionTypeCount() {
        Long count = competitionTypeMapper.selectCount(null);
        return Response.of(count);
    }

    @Override
    public Response getAllCompetitionType(int pageNow, int pageSize) {
        Page<CompetitionType> page = new Page<>(pageNow, pageSize);
        Page<CompetitionType> competitionTypePage = competitionTypeMapper.selectPage(page, null);
        List<CompetitionType> competitionTypes = competitionTypePage.getRecords();
        return Response.of(competitionTypes);
    }

    @Override
    public Response addCompetitionType(String typeName) {
        LambdaQueryWrapper<CompetitionType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompetitionType::getTypeName, typeName).select(CompetitionType::getId, CompetitionType::getTypeName);
        CompetitionType competitionType = competitionTypeMapper.selectOne(queryWrapper);
        if (competitionType != null) {
            return Response.error(ErrorCode.DATA_EXIST.getErrCode(), "已有同名比赛类型");
        }
        CompetitionType competitionTypeNew = new CompetitionType();
        competitionTypeNew.setTypeName(typeName);
        competitionTypeNew.setId(UUID.randomUUID().toString());
        int res = competitionTypeMapper.insert(competitionTypeNew);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.INSERT_FAILED.getErrCode(), "新增比赛类型失败");
        }
    }

    @Override
    public Response updateCompetitionType(String id, String typeName) {
        LambdaQueryWrapper<CompetitionType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompetitionType::getId, id).select(CompetitionType::getId);
        CompetitionType competitionType = competitionTypeMapper.selectOne(queryWrapper);
        if (competitionType == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛类型不存在");
        }
        competitionType.setTypeName(typeName);
        int res = competitionTypeMapper.updateById(competitionType);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.UPDATE_FAILED.getErrCode(), "更新比赛类型失败");
        }
    }

    @Override
    public Response getCompetitionGroupCategoryByCompetitionId(String competitionId) {
        LambdaQueryWrapper<CompetitionGroupCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompetitionGroupCategory::getCompetitionId, competitionId);
        List<CompetitionGroupCategory> competitionGroupCategories = competitionGroupCategoryMapper.selectList(queryWrapper);
        return Response.of(competitionGroupCategories);
    }

    @Override
    public Response updateCompetitionGroupCategory(String id, String name) {
        CompetitionGroupCategory competitionGroupCategory = competitionGroupCategoryMapper.selectById(id);
        if (competitionGroupCategory == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛组别不存在");
        }

        LambdaQueryWrapper<CompetitionGroupCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(CompetitionGroupCategory::getCompetitionId, competitionGroupCategory.getCompetitionId())
                .eq(CompetitionGroupCategory::getName, name).select(CompetitionGroupCategory::getId);
        CompetitionGroupCategory competitionGroupCategoryByName = competitionGroupCategoryMapper.selectOne(queryWrapper);
        if (competitionGroupCategoryByName != null) {
            return Response.error(ErrorCode.DATA_EXIST.getErrCode(), "该比赛已有同名分组");
        }

        competitionGroupCategory.setName(name);
        int res = competitionGroupCategoryMapper.updateById(competitionGroupCategory);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.UPDATE_FAILED.getErrCode(), "更新比赛组别失败");
        }
    }

    @Override
    public Response addCompetitionGroupCategory(String competitionId, String name) {
        LambdaQueryWrapper<CompetitionGroupCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompetitionGroupCategory::getCompetitionId, competitionId).eq(CompetitionGroupCategory::getName, name).select(CompetitionGroupCategory::getId);
        CompetitionGroupCategory competitionGroupCategory = competitionGroupCategoryMapper.selectOne(queryWrapper);
        if (competitionGroupCategory != null) {
            return Response.error(ErrorCode.DATA_EXIST.getErrCode(), "该比赛已有同名分组");
        }
        CompetitionGroupCategory competitionGroupCategoryNew = new CompetitionGroupCategory();
        competitionGroupCategoryNew.setCompetitionId(competitionId);
        competitionGroupCategoryNew.setName(name);
        competitionGroupCategoryNew.setId(UUID.randomUUID().toString());
        int res = competitionGroupCategoryMapper.insert(competitionGroupCategoryNew);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.INSERT_FAILED.getErrCode(), "添加分组失败");
        }
    }

    @Override
    public Response getAllCompetition(AdminGetAllCompetitionRequest adminGetAllCompetitionRequest) {
        LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();

        // 承办学校过滤
        String keywordSchool = adminGetAllCompetitionRequest.getKeywordSchool();
        if (keywordSchool != null && !keywordSchool.isEmpty()) {
            LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
            queryWrapperSchool.like(School::getName, keywordSchool).select(School::getId);
            List<School> schools = schoolMapper.selectList(queryWrapperSchool);
            List<String> schoolIds = schools.stream().map(School::getId).toList();
            if (!schoolIds.isEmpty()) {
                queryWrapper.and(qw -> qw.in(Competition::getSchoolId, schoolIds));
            } else {
                return Response.of(new ArrayList<>());
            }
        }

        // 承办人过滤
        String keywordCompetitionAdmin = adminGetAllCompetitionRequest.getKeywordCompetitionAdmin();
        if (keywordCompetitionAdmin != null && !keywordCompetitionAdmin.isEmpty()) {
            LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
            queryWrapperUser.apply("CONCAT(first_name, last_name) LIKE {0}",
                            "%" + keywordCompetitionAdmin + "%")
                    .select(User::getId);
            List<User> users = userMapper.selectList(queryWrapperUser);
            List<String> userIds = users.stream().map(User::getId).toList();
            if (!userIds.isEmpty()) {
                queryWrapper.and(qw -> qw.in(Competition::getCompetitionAdminId, userIds));
            } else {
                return Response.of(new ArrayList<>());
            }
        }

        // 比赛名过滤
        String keywordName = adminGetAllCompetitionRequest.getKeywordName();
        if (keywordName != null && !keywordName.isEmpty()) {
            queryWrapper.and(qw -> qw.like(Competition::getName, keywordName));
        }

        // 类型过滤
        List<String> keywordTypes = adminGetAllCompetitionRequest.getKeywordTypes();
        if (keywordTypes != null && !keywordTypes.isEmpty()) {
            queryWrapper.and(qw -> qw.in(Competition::getType, keywordTypes));
        }

        // 赛季过滤
        List<String> keywordSeasons = adminGetAllCompetitionRequest.getKeywordSeasons();
        if (keywordSeasons != null && !keywordSeasons.isEmpty()) {
            queryWrapper.and(qw -> qw.in(Competition::getSeason, keywordSeasons));
        }

        int pageNow = adminGetAllCompetitionRequest.getPageNow();
        int pageSize = adminGetAllCompetitionRequest.getPageSize();
        Page<Competition> page = new Page<>(pageNow, pageSize);
        Page<Competition> competitionPage = competitionMapper.selectPage(page, queryWrapper);
        List<Competition> competitions = competitionPage.getRecords();

        List<AdminGetAllCompetitionResponse> adminGetAllCompetitionResponses = new ArrayList<>();
        for (Competition competition : competitions) {
            AdminGetAllCompetitionResponse adminGetAllCompetitionResponse = new AdminGetAllCompetitionResponse();
            BeanUtils.copyProperties(competition, adminGetAllCompetitionResponse);
            // 查询学校名
            String schoolId = competition.getSchoolId();
            if (schoolId!= null && !schoolId.isEmpty()) {
                LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
                queryWrapperSchool.eq(School::getId, schoolId).select(School::getId, School::getName);
                School school = schoolMapper.selectOne(queryWrapperSchool);
                if (school == null) {
                    return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "查询承办学校失败");
                }
                adminGetAllCompetitionResponse.setSchoolName(school.getName());
            }
            // 查询管理员名
            String competitionAdminId = competition.getCompetitionAdminId();
            if (competitionAdminId != null && !competitionAdminId.isEmpty()) {
                LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
                queryWrapperUser.eq(User::getId, competitionAdminId).select(User::getId, User::getFirstName, User::getLastName);
                User user = userMapper.selectOne(queryWrapperUser);
                if (user == null) {
                    return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "查询比赛管理员失败");
                }
                adminGetAllCompetitionResponse.setCompetitionAdminName(user.getFirstName() + user.getLastName());
            }

            adminGetAllCompetitionResponses.add(adminGetAllCompetitionResponse);
        }
        return Response.of(adminGetAllCompetitionResponses);
    }

    @Override
    public Response getCompetitionCount(AdminGetCompetitionCountRequest adminGetCompetitionCountRequest) {
        LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();

        // 承办学校过滤
        String keywordSchool = adminGetCompetitionCountRequest.getKeywordSchool();
        if (keywordSchool != null && !keywordSchool.isEmpty()) {
            LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
            queryWrapperSchool.like(School::getName, keywordSchool).select(School::getId);
            List<School> schools = schoolMapper.selectList(queryWrapperSchool);
            List<String> schoolIds = schools.stream().map(School::getId).toList();
            if (!schoolIds.isEmpty()) {
                queryWrapper.and(qw -> qw.in(Competition::getSchoolId, schoolIds));
            } else {
                return Response.of(new ArrayList<>());
            }
        }

        // 承办人过滤
        String keywordCompetitionAdmin = adminGetCompetitionCountRequest.getKeywordCompetitionAdmin();
        if (keywordCompetitionAdmin != null && !keywordCompetitionAdmin.isEmpty()) {
            LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
            queryWrapperUser.apply("CONCAT(first_name, last_name) LIKE {0}",
                            "%" + keywordCompetitionAdmin + "%")
                    .select(User::getId);
            List<User> users = userMapper.selectList(queryWrapperUser);
            List<String> userIds = users.stream().map(User::getId).toList();
            if (!userIds.isEmpty()) {
                queryWrapper.and(qw -> qw.in(Competition::getCompetitionAdminId, userIds));
            } else {
                return Response.of(new ArrayList<>());
            }
        }

        // 比赛名过滤
        String keywordName = adminGetCompetitionCountRequest.getKeywordName();
        if (keywordName != null && !keywordName.isEmpty()) {
            queryWrapper.and(qw -> qw.like(Competition::getName, keywordName));
        }

        // 类型过滤
        List<String> keywordTypes = adminGetCompetitionCountRequest.getKeywordTypes();
        if (keywordTypes != null && !keywordTypes.isEmpty()) {
            queryWrapper.and(qw -> qw.in(Competition::getType, keywordTypes));
        }

        // 赛季过滤
        List<String> keywordSeasons = adminGetCompetitionCountRequest.getKeywordSeasons();
        if (keywordSeasons != null && !keywordSeasons.isEmpty()) {
            queryWrapper.and(qw -> qw.in(Competition::getSeason, keywordSeasons));
        }

        Long count = competitionMapper.selectCount(queryWrapper);
        return Response.of(count);
    }

    @Override
    public Response getCompetitionInfoById(String competitionId) {
        LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Competition::getId, competitionId);
        Competition competition = competitionMapper.selectOne(queryWrapper);
        if (competition == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }
        AdminGetCompetitionInfoByIdResponse
                adminGetCompetitionInfoByIdResponse = new AdminGetCompetitionInfoByIdResponse();
        BeanUtils.copyProperties(competition, adminGetCompetitionInfoByIdResponse);

        // 查询学校名
        String schoolId = competition.getSchoolId();
        if (schoolId != null && !schoolId.isEmpty()) {
            LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
            queryWrapperSchool.eq(School::getId, schoolId).select(School::getId, School::getName);
            School school = schoolMapper.selectOne(queryWrapperSchool);
            if (school == null) {
                return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "查询承办学校失败");
            }
            adminGetCompetitionInfoByIdResponse.setSchoolName(school.getName());
        }
        // 查询管理员名
        String competitionAdminId = competition.getCompetitionAdminId();
        if (competitionAdminId != null && !competitionAdminId.isEmpty()) {
            LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
            queryWrapperUser.eq(User::getId, competitionAdminId).select(User::getId, User::getFirstName, User::getLastName);
            User user = userMapper.selectOne(queryWrapperUser);
            if (user == null) {
                return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "查询比赛管理员失败");
            }
            adminGetCompetitionInfoByIdResponse.setCompetitionAdminName(user.getFirstName() + user.getLastName());
        }

        return Response.of(adminGetCompetitionInfoByIdResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response updateCompetitionInfoById(AdminUpdateCompetitionInfoByIdRequest adminUpdateCompetitionInfoByIdRequest) {
        String competitionAdminId = adminUpdateCompetitionInfoByIdRequest.getCompetitionAdminId();
        String schoolId = adminUpdateCompetitionInfoByIdRequest.getSchoolId();
        if (competitionAdminId != null && schoolId != null) {
            LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
            queryWrapperUser.eq(User::getId, competitionAdminId).select(User::getId, User::getSchoolId);
            User user = userMapper.selectOne(queryWrapperUser);
            if (user == null) {
                throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "所选比赛负责人不存在");
            }
            if (!user.getSchoolId().equals(schoolId)) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "所选比赛负责人不属于承办院校");
            }
            List<String> roleIds = userMapper.getAllRoleByUserId(competitionAdminId);
            if (roleIds == null) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "所选比赛负责人身份错误");
            }
            if (!roleIds.contains("5")) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "所选用户不是比赛负责人");
            }
        } else if (competitionAdminId != null) {
            throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "请先选择承办学校");
        }

        Competition competition = new Competition();
        BeanUtils.copyProperties(adminUpdateCompetitionInfoByIdRequest, competition);
        competition.setUpdatedAt(TimeUtil.getCurrentTimestamp());
        int result = competitionMapper.updateById(competition);
        if (result != 1) {
            throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "更新比赛信息失败");
        }
        // 更新比赛组别
        List<CompetitionGroupCategory> competitionGroupCategories = adminUpdateCompetitionInfoByIdRequest.getCompetitionGroupCategories();
        for (CompetitionGroupCategory competitionGroupCategory : competitionGroupCategories) {
            if (!competitionGroupCategory.getId().isEmpty()) {
                int res = competitionGroupCategoryMapper.updateById(competitionGroupCategory);
                if (res != 1) {
                    throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "更新比赛组别失败");
                }
            } else {
                competitionGroupCategory.setId(UUID.randomUUID().toString());
                int res = competitionGroupCategoryMapper.insert(competitionGroupCategory);
                if (res != 1) {
                    throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "添加比赛组别失败");
                }
            }
        }
        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response addCompetition(AdminAddCompetitionRequest adminAddCompetitionRequest) {
        String competitionAdminId = adminAddCompetitionRequest.getCompetitionAdminId();
        String schoolId = adminAddCompetitionRequest.getSchoolId();
        if (competitionAdminId != null && schoolId != null) {
            LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
            queryWrapperUser.eq(User::getId, competitionAdminId).select(User::getId, User::getSchoolId);
            User user = userMapper.selectOne(queryWrapperUser);
            if (user == null) {
                throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "所选比赛负责人不存在");
            }
            if (!user.getSchoolId().equals(schoolId)) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "所选比赛负责人不属于承办院校");
            }
            List<String> roleIds = userMapper.getAllRoleByUserId(competitionAdminId);
            if (roleIds == null) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "所选比赛负责人身份错误");
            }
            if (!roleIds.contains("5")) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "所选用户不是比赛负责人");
            }
        } else if (competitionAdminId != null) {
            throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "请先选择承办学校");
        }

        Competition competition = new Competition();
        BeanUtils.copyProperties(adminAddCompetitionRequest, competition);
        competition.setId(UUID.randomUUID().toString());
        long timestamp = TimeUtil.getCurrentTimestamp();
        competition.setCreatedAt(timestamp);
        competition.setUpdatedAt(timestamp);
        int insert = competitionMapper.insert(competition);
        if (insert != 1) {
            throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "新建比赛失败");
        }

        // 添加比赛组别
        List<CompetitionGroupCategory> competitionGroupCategories = adminAddCompetitionRequest.getCompetitionGroupCategories();
        for (CompetitionGroupCategory competitionGroupCategory : competitionGroupCategories) {
            competitionGroupCategory.setCompetitionId(competition.getId());
            competitionGroupCategory.setId(UUID.randomUUID().toString());
            int res = competitionGroupCategoryMapper.insert(competitionGroupCategory);
            if (res != 1) {
                throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "添加比赛组别失败");
            }
        }
        return Response.success();
    }

    @Override
    public Response getAllCompetitionAdminBySchool(String schoolId) {
        List<SchoolGetAllCompetitionAdminVo> competitionAdmins = userMapper.getAllCompetitionAdminBySchool(schoolId);
        if (competitionAdmins == null) {
            return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "查询学校管理员失败");
        }
        return Response.of(competitionAdmins);
    }

    @Override
    public Response getAllSchool(AdminGetAllSchoolRequest adminGetAllSchoolRequest) {
        LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(School::getId, School::getName, School::getNameEn, School::getMailingAddress, School::getSchoolBadgeUrl);
        // 学校名关键字
        String keywordName = adminGetAllSchoolRequest.getKeywordName();
        if (keywordName != null && !keywordName.isEmpty()) {
            queryWrapper.and(qw -> qw.like(School::getName, keywordName));
        }
        // 学校英文名关键字
        String keywordNameEn = adminGetAllSchoolRequest.getKeywordNameEn();
        if (keywordNameEn != null && !keywordNameEn.isEmpty()) {
            queryWrapper.and(qw -> qw.like(School::getNameEn, keywordNameEn));
        }
        // 通信地址关键字
        String keywordMailingAddress = adminGetAllSchoolRequest.getKeywordMailingAddress();
        if (keywordMailingAddress != null && !keywordMailingAddress.isEmpty()) {
            queryWrapper.and(qw -> qw.like(School::getMailingAddress, keywordMailingAddress));
        }

        int pageNow = adminGetAllSchoolRequest.getPageNow();
        int pageSize = adminGetAllSchoolRequest.getPageSize();
        Page<School> page = new Page<>(pageNow, pageSize);
        Page<School> schoolPage = schoolMapper.selectPage(page, queryWrapper);
        List<School> records = schoolPage.getRecords();

        List<AdminGetAllSchoolResponse> adminGetAllSchoolResponses = new ArrayList<>();
        for (School school : records) {
            AdminGetAllSchoolResponse adminGetAllSchoolResponse = new AdminGetAllSchoolResponse();
            BeanUtils.copyProperties(school, adminGetAllSchoolResponse);
            adminGetAllSchoolResponses.add(adminGetAllSchoolResponse);
        }
        return Response.of(adminGetAllSchoolResponses);
    }

    @Override
    public Response getSchoolCount(AdminGetSchoolCountRequest adminGetSchoolCountRequest) {
        LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
        // 学校名关键字
        String keywordName = adminGetSchoolCountRequest.getKeywordName();
        if (keywordName != null && !keywordName.isEmpty()) {
            queryWrapper.and(qw -> qw.like(School::getName, keywordName));
        }
        // 学校英文名关键字
        String keywordNameEn = adminGetSchoolCountRequest.getKeywordNameEn();
        if (keywordNameEn != null && !keywordNameEn.isEmpty()) {
            queryWrapper.and(qw -> qw.like(School::getNameEn, keywordNameEn));
        }
        // 通信地址关键字
        String keywordMailingAddress = adminGetSchoolCountRequest.getKeywordMailingAddress();
        if (keywordMailingAddress != null && !keywordMailingAddress.isEmpty()) {
            queryWrapper.and(qw -> qw.like(School::getMailingAddress, keywordMailingAddress));
        }
        Long count = schoolMapper.selectCount(queryWrapper);
        return Response.of(count);
    }

    @Override
    public Response getSchoolInfoById(String schoolId) {
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "该学校不存在");
        }
        return Response.of(school);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response updateSchoolInfoById(AdminUpdateSchoolInfoByIdRequest adminUpdateSchoolInfoByIdRequest) {
        String schoolId = adminUpdateSchoolInfoByIdRequest.getId();
        LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(School::getId, schoolId).select(School::getId, School::getSchoolAdminId);
        School schoolWithAdmin = schoolMapper.selectOne(queryWrapper);
        if (schoolWithAdmin == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "学校不存在");
        }

        // 管理员身份变化
        String oldSchoolAdminId = schoolWithAdmin.getSchoolAdminId();
        String newSchoolAdminId = adminUpdateSchoolInfoByIdRequest.getSchoolAdminId();
        if (oldSchoolAdminId != null && newSchoolAdminId != null && !newSchoolAdminId.equals(oldSchoolAdminId)) {
            // 删除旧管理员身份
            LambdaQueryWrapper<UserRole> oldRoleQueryWrapper = new LambdaQueryWrapper<>();
            oldRoleQueryWrapper.eq(UserRole::getUserId, oldSchoolAdminId).eq(UserRole::getRoleId, "4");
            int oldDeleteResult = userRoleMapper.delete(oldRoleQueryWrapper);
            if (oldDeleteResult != 1) {
                throw new BizException(ErrorCode.DELETE_FAILED.getErrCode(), "删除旧管理员身份失败");
            }
            // 判断新的管理员存在
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.eq(User::getId, newSchoolAdminId);
            User newSchoolAdmin = userMapper.selectOne(userQueryWrapper);
            if (newSchoolAdmin == null) {
                throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "所选管理员不存在");
            }
            // 增加新管理员身份
            UserRole newUserRole = new UserRole();
            newUserRole.setUserId(newSchoolAdminId);
            newUserRole.setRoleId("4");
            newUserRole.setId(UUID.randomUUID().toString());
            int newInsertResult = userRoleMapper.insert(newUserRole);
            if (newInsertResult != 1) {
                throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "增加新管理员身份失败");
            }
        } else if (oldSchoolAdminId != null && newSchoolAdminId == null) {
            // 删除旧管理员身份
            LambdaQueryWrapper<UserRole> oldRoleQueryWrapper = new LambdaQueryWrapper<>();
            oldRoleQueryWrapper.eq(UserRole::getUserId, oldSchoolAdminId).eq(UserRole::getRoleId, "4");
            int oldDeleteResult = userRoleMapper.delete(oldRoleQueryWrapper);
            if (oldDeleteResult != 1) {
                throw new BizException(ErrorCode.DELETE_FAILED.getErrCode(), "删除旧管理员身份失败");
            }
        } else if (oldSchoolAdminId == null && newSchoolAdminId != null) {
            // 判断新的管理员存在
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.eq(User::getId, newSchoolAdminId);
            User newSchoolAdmin = userMapper.selectOne(userQueryWrapper);
            if (newSchoolAdmin == null) {
                throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "所选管理员不存在");
            }
            // 增加新管理员身份
            UserRole newUserRole = new UserRole();
            newUserRole.setUserId(newSchoolAdminId);
            newUserRole.setRoleId("4");
            newUserRole.setId(UUID.randomUUID().toString());
            int newInsertResult = userRoleMapper.insert(newUserRole);
            if (newInsertResult != 1) {
                throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "增加新管理员身份失败");
            }
        }

        // 更新学校信息
        School school = new School();
        BeanUtils.copyProperties(adminUpdateSchoolInfoByIdRequest, school);
        int result = schoolMapper.updateById(school);
        if (result != 1) {
            throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "更新学校信息失败");
        }
        return Response.success();
    }

    @Override
    public Response addSchool(AdminAddSchoolRequest adminAddSchoolRequest) {
        School school = new School();
        BeanUtils.copyProperties(adminAddSchoolRequest, school);
        school.setId(UUID.randomUUID().toString());
        int result = schoolMapper.insert(school);
        if (result != 1) {
            throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "添加学校失败");
        }
        return Response.success();
    }

    @Override
    public Response getAllCoachBySchool(String schoolId) {
        List<SchoolGetAllCoachVo> coaches = userMapper.getAllCoachBySchool(schoolId);
        if (coaches == null) {
            return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "查询学校所有教练失败");
        }
        return Response.of(coaches);
    }

    @Override
    public Response getCurrentSeason() {
        LambdaQueryWrapper<Season> seasonQueryWrapper = new LambdaQueryWrapper<>();
        seasonQueryWrapper.eq(Season::getIsCurrentSeason, true).select(Season::getId);
        Season season = seasonMapper.selectOne(seasonQueryWrapper);
        return Response.of(season.getId());
    }

    @Override
    public Response getSeasonCount() {
        Long count = seasonMapper.selectCount(null);
        return Response.of(count);
    }

    @Override
    public Response getAllSeason(int pageNow, int pageSize) {
        Page<Season> page = new Page<>(pageNow, pageSize);
        LambdaQueryWrapper<Season> seasonQueryWrapper = new LambdaQueryWrapper<>();
        seasonQueryWrapper.orderByDesc(Season::getId);
        Page<Season> seasonPage = seasonMapper.selectPage(page, seasonQueryWrapper);
        List<Season> records = seasonPage.getRecords();
        return Response.of(records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response setCurrentSeason(String seasonId) {
        LambdaQueryWrapper<Season> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Season::getIsCurrentSeason, true).select(Season::getId, Season::getIsCurrentSeason);
        Season season = seasonMapper.selectOne(queryWrapper);
        if (season != null) {
            season.setIsCurrentSeason(false);
            int res1 = seasonMapper.updateById(season);

            Season currentSeason = new Season();
            currentSeason.setId(seasonId);
            currentSeason.setIsCurrentSeason(true);
            int res2 = seasonMapper.updateById(currentSeason);

            if (res1 == 1 && res2 == 1) {
                return Response.success();
            } else {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "更新当前赛季失败");
            }
        } else {
            Season currentSeason = new Season();
            currentSeason.setId(seasonId);
            currentSeason.setIsCurrentSeason(true);
            int res = seasonMapper.updateById(currentSeason);
            if (res == 1) {
                return Response.success();
            } else {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "更新当前赛季失败");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response addSeason(String seasonId, Boolean isCurrentSeason) {
        if (!isCurrentSeason) {
            Season currentSeason = new Season();
            currentSeason.setId(seasonId);
            currentSeason.setIsCurrentSeason(false);
            int res = seasonMapper.insert(currentSeason);
            if (res == 1) {
                return Response.success();
            } else {
                throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "新增赛季失败");
            }
        } else {
            LambdaQueryWrapper<Season> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Season::getIsCurrentSeason, true).select(Season::getId, Season::getIsCurrentSeason);
            Season season = seasonMapper.selectOne(queryWrapper);
            if (season != null) {
                season.setIsCurrentSeason(false);
                int res1 = seasonMapper.updateById(season);

                Season currentSeason = new Season();
                currentSeason.setId(seasonId);
                currentSeason.setIsCurrentSeason(true);
                int res2 = seasonMapper.insert(currentSeason);
                if (res1 == 1 && res2 == 1) {
                    return Response.success();
                } else {
                    throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "新增赛季失败");
                }
            } else {
                Season currentSeason = new Season();
                currentSeason.setId(seasonId);
                currentSeason.setIsCurrentSeason(true);
                int res = seasonMapper.insert(currentSeason);
                if (res == 1) {
                    return Response.success();
                } else {
                    throw new BizException(ErrorCode.INSERT_FAILED.getErrCode(), "新增赛季失败");
                }
            }
        }
    }

    @Override
    public Response updateSeason(String newSeasonId, String oldSeasonId) {
        LambdaQueryWrapper<Season> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Season::getId, oldSeasonId).select(Season::getId, Season::getIsCurrentSeason);
        Season season = seasonMapper.selectOne(queryWrapper);
        if (season == null) {
            throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "更新赛季失败");
        } else {
            Season newSeason = new Season();
            newSeason.setId(newSeasonId);
            newSeason.setIsCurrentSeason(season.getIsCurrentSeason());

            int res1 = seasonMapper.deleteById(oldSeasonId);
            int res2 = seasonMapper.insert(newSeason);
            if (res1 == 1 && res2 == 1) {
                return Response.success();
            } else {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "更新赛季失败");
            }
        }
    }

    @Override
    public Response getSystemInfo() {
        SystemInfo systemInfo = systemInfoMapper.selectById("1");
        if (systemInfo != null) {
            return Response.of(systemInfo);
        } else {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "系统信息不存在");
        }
    }

    @Override
    public Response updateSystemInfo(AdminUpdateSystemInfoRequest adminUpdateSystemInfoRequest) {
        SystemInfo systemInfo = new SystemInfo();
        BeanUtils.copyProperties(adminUpdateSystemInfoRequest, systemInfo);
        systemInfo.setId("1");
        int res = systemInfoMapper.updateById(systemInfo);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.UPDATE_FAILED.getErrCode(), "更新系统信息失败");
        }
    }

    @Override
    public Response getAllSlideshow(int pageNow, int pageSize) {
        Long count = slideshowMapper.selectCount(null);

        Page<Slideshow> page = new Page<>(pageNow, pageSize);
        Page<Slideshow> slideshowPage = slideshowMapper.selectPage(page, null);
        List<Slideshow> slideshows = slideshowPage.getRecords();
        AdminGetAllSlideshowResponse response = new AdminGetAllSlideshowResponse();
        response.setCount(count);
        response.setSlideshows(slideshows);
        return Response.of(response);
    }

    @Override
    public Response addSlideshow(AdminAddSlideshowRequest adminAddSlideshowRequest) {
        Slideshow slideshow = new Slideshow();
        BeanUtils.copyProperties(adminAddSlideshowRequest, slideshow);
        slideshow.setId(UUID.randomUUID().toString());
        int res = slideshowMapper.insert(slideshow);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.INSERT_FAILED.getErrCode(), "添加轮播图失败");
        }
    }

    @Override
    public Response updateSlideshow(AdminUpdateSlideshowRequest adminUpdateSlideshowRequest) {
        Slideshow slideshow = slideshowMapper.selectById(adminUpdateSlideshowRequest.getId());
        if (slideshow == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "轮播图不存在");
        } else {
            Slideshow updateSlideshow = new Slideshow();
            BeanUtils.copyProperties(adminUpdateSlideshowRequest, updateSlideshow);
            int res = slideshowMapper.updateById(updateSlideshow);
            if (res == 1) {
                return Response.success();
            } else {
                return Response.error(ErrorCode.UPDATE_FAILED.getErrCode(), "更新轮播图失败");
            }
        }
    }

    @Override
    public Response deleteSlideshow(String slideshowId) {
        Slideshow slideshow = slideshowMapper.selectById(slideshowId);
        if (slideshow == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "轮播图不存在");
        } else {
            int res = slideshowMapper.deleteById(slideshowId);
            if (res == 1) {
                return Response.success();
            } else {
                return Response.error(ErrorCode.DELETE_FAILED.getErrCode(), "删除轮播图失败");
            }
        }
    }

    public Response getAllAnnouncement1(AdminGetAllAnnouncementRequest adminGetAllAnnouncementRequest) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        AdminGetAllAnnouncementResponse ans = new AdminGetAllAnnouncementResponse();

        // 作者筛选条件
        String keywordAuthor = adminGetAllAnnouncementRequest.getKeywordAuthor();
        if (keywordAuthor != null && !keywordAuthor.isEmpty()) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.apply("CONCAT(first_name, last_name) LIKE {0}", "%" + keywordAuthor + "%");
            List<User> users = userMapper.selectList(userQueryWrapper);
            List<String> userIds = users.stream().map(User::getId).toList();
            if (!userIds.isEmpty()) {
                queryWrapper.and(qw -> qw.in(Announcement::getAuthorId, userIds));
            } else {
                ans.setCount(0);
                ans.setAnnouncements(new ArrayList<>());
                return Response.of(ans);
            }
        }

        // 标题筛选条件
        String keywordTitle = adminGetAllAnnouncementRequest.getKeywordTitle();
        if (keywordTitle != null && !keywordTitle.isEmpty()) {
            queryWrapper.and(qw -> qw.like(Announcement::getTitle, keywordTitle));
        }

        // 状态筛选条件
        List<Integer> keywordStatuses = adminGetAllAnnouncementRequest.getKeywordStatuses();
        if (keywordStatuses != null && !keywordStatuses.isEmpty()) {
            queryWrapper.and(qw -> qw.in(Announcement::getStatus, keywordStatuses));
        }

        // 赛季筛选条件
        List<String> keywordSeasons = adminGetAllAnnouncementRequest.getKeywordSeasons();
        if (keywordSeasons != null && !keywordSeasons.isEmpty()) {
            queryWrapper.and(qw -> qw.in(Announcement::getSeason, keywordSeasons));
        }

        int pageNow = adminGetAllAnnouncementRequest.getPageNow();
        int pageSize = adminGetAllAnnouncementRequest.getPageSize();
        Page<Announcement> page = new Page<>(pageNow, pageSize);
        long count = announcementMapper.selectCount(queryWrapper);
        Page<Announcement> announcementPage = announcementMapper.selectPage(page, queryWrapper);
        List<Announcement> announcements = announcementPage.getRecords();
        ans.setCount(count);
        return Response.of(ans);
    }

    @Override
    public Response getAllAnnouncement(AdminGetAllAnnouncementRequest adminGetAllAnnouncementRequest) {
        AdminGetAllAnnouncementResponse ans = new AdminGetAllAnnouncementResponse();

        Map<String, Object> params = new HashMap<>();
        params.put("keywordAuthor", adminGetAllAnnouncementRequest.getKeywordAuthor());
        params.put("keywordTitle", adminGetAllAnnouncementRequest.getKeywordTitle());
        params.put("keywordStatuses", adminGetAllAnnouncementRequest.getKeywordStatuses());
        params.put("keywordSeasons", adminGetAllAnnouncementRequest.getKeywordSeasons());
        params.put("pageNow", adminGetAllAnnouncementRequest.getPageNow());
        params.put("pageSize", adminGetAllAnnouncementRequest.getPageSize());
        params.put("offset", adminGetAllAnnouncementRequest.getPageSize() * (adminGetAllAnnouncementRequest.getPageNow() - 1));

        long count = announcementMapper.getAnnouncementsCount(params);
        List<AdminGetAllAnnouncementVo> announcements = announcementMapper.getAllAnnouncement(params);
        ans.setCount(count);
        ans.setAnnouncements(announcements);
        return Response.of(ans);
    }

    @Override
    public Response getAnnouncementInfo(String announcementId) {
        AdminGetAnnouncementInfoResponse announcement = announcementMapper.getAnnouncementInfo(announcementId);
        if (announcement == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "获取公告信息失败");
        }
        return Response.of(announcement);
    }

    @Override
    public Response addAnnouncement(AdminAddAnnouncementRequest adminAddAnnouncementRequest) {
        String id = RoleUtil.getCurrentUserId();

        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(adminAddAnnouncementRequest, announcement);
        announcement.setId(UUID.randomUUID().toString());
        announcement.setAuthorId(id);
        announcement.setViewCount(0L);
        announcement.setCreateAt(TimeUtil.getCurrentTimestamp());
        announcement.setUpdateAt(TimeUtil.getCurrentTimestamp());

        int res = announcementMapper.insert(announcement);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.INSERT_FAILED.getErrCode(), "添加公告失败");
        }
    }

    @Override
    public Response updateAnnouncement(AdminUpdateAnnouncementRequest adminUpdateAnnouncementRequest) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(adminUpdateAnnouncementRequest, announcement);
        announcement.setUpdateAt(TimeUtil.getCurrentTimestamp());
        int res = announcementMapper.updateById(announcement);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.UPDATE_FAILED.getErrCode(), "更新公告失败");
        }
    }

    @Override
    public Response getAllSponsor(AdminGetAllSponsorRequest adminGetAllSponsorRequest) {
        LambdaQueryWrapper<Sponsor> queryWrapper = new LambdaQueryWrapper<>();

        // 名称过滤
        String keywordName = adminGetAllSponsorRequest.getKeywordName();
        if (keywordName != null && !keywordName.isEmpty()) {
            queryWrapper.and(qw -> qw.like(Sponsor::getName, keywordName));
        }

        // 类型过滤
        List<Integer> keywordTypes = adminGetAllSponsorRequest.getKeywordTypes();
        if (keywordTypes != null && !keywordTypes.isEmpty()) {
            queryWrapper.and(qw -> qw.in(Sponsor::getType, keywordTypes));
        }

        // 是否展示过滤
        List<Boolean> keywordShows = adminGetAllSponsorRequest.getKeywordShows();
        if (keywordShows != null && !keywordShows.isEmpty()) {
            queryWrapper.and(qw -> qw.in(Sponsor::getShow, keywordShows));
        }

        long count = sponsorMapper.selectCount(queryWrapper);
        int pageNow = adminGetAllSponsorRequest.getPageNow();
        int pageSize = adminGetAllSponsorRequest.getPageSize();
        Page<Sponsor> page = new Page<>(pageNow, pageSize);
        Page<Sponsor> sponsorPage = sponsorMapper.selectPage(page, queryWrapper);
        List<Sponsor> sponsors = sponsorPage.getRecords();

        AdminGetAllSponsorResponse adminGetAllSponsorResponse = new AdminGetAllSponsorResponse();
        adminGetAllSponsorResponse.setSponsors(sponsors);
        adminGetAllSponsorResponse.setCount(count);
        return Response.of(adminGetAllSponsorResponse);
    }

    @Override
    public Response addSponsor(Sponsor sponsor) {
        sponsor.setId(UUID.randomUUID().toString());
        int res = sponsorMapper.insert(sponsor);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.INSERT_FAILED.getErrCode(), "添加赞助商失败");
        }
    }

    @Override
    public Response updateSponsor(Sponsor sponsor) {
        int res = sponsorMapper.updateById(sponsor);
        if (res == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.UPDATE_FAILED.getErrCode(), "更新赞助商失败");
        }
    }

    @Override
    public void getTestExcelFile(HttpServletResponse response) {
        try {
            List<List<String>> ans = new ArrayList<>();
            ans.add(new ArrayList<>());
            ans.getFirst().add("123");
            ans.getFirst().add("456");
            ans.getFirst().add("789");
            byte[] bytes = ExcelUtil.generateExcel(ans);

            String fileName = "嘟嘟哒嘟嘟哒.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            response.setContentType("application/octet-stream;character=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setContentLength(bytes.length);

            // 禁止缓存
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // 将二进制文件写入输出流
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                outputStream.write(bytes);
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new BizException(ErrorCode.DOWNLOAD_FAILED.getErrCode(), "下载文件失败");
        }
    }

    @Override
    public Response uploadTestFile(MultipartFile file, String param) {
        String fileName = file.getOriginalFilename();
        String fileType = FileUtil.extName(fileName);
        return Response.of(fileType + param);
    }

}
