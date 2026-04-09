
package com.neuq.ccpcbackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.entity.*;
import com.neuq.ccpcbackend.mapper.*;
import com.neuq.ccpcbackend.properties.KeyProperties;
import com.neuq.ccpcbackend.service.UserService;
import com.neuq.ccpcbackend.utils.*;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.response.Response;
import com.neuq.ccpcbackend.vo.UserCacheVo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Resource;

import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    UserMapper userMapper;

    @Resource
    RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    SchoolMapper schoolMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Resource
    private TeamMapper teamMapper;


    private UserTokenResponse generateToken(String id, List<String> identities, UserGetBaseInfoResponse userGetBaseInfoResponse) {
        UserCacheVo userCacheVo = new UserCacheVo();
        userCacheVo.setId(id);
        userCacheVo.setIdentities(identities);

        // 5分钟过期
        String accessToken = JwtUtil.generateToken(JSON.toJSONString(userCacheVo), 10 * 1000L);
        // 1天过期
        String refreshToken = JwtUtil.generateToken(id, 60 * 60 * 24 * 1000L);

        UserTokenResponse userTokenResponse = new UserTokenResponse();
        userTokenResponse.setAccessToken(accessToken);
        userTokenResponse.setRefreshToken(refreshToken);
        userTokenResponse.setRoles(identities);
        userTokenResponse.setUserInfo(userGetBaseInfoResponse);
        return userTokenResponse;
    }

    @Override
    public Response loginWithCode(UserLoginWithCodeRequest userLoginWithCodeRequest) {
        String phone = userLoginWithCodeRequest.getPhone();
        String code = userLoginWithCodeRequest.getCode();

        // 根据手机号获取id
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone).select(User::getId);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "手机号未注册");
        }

        // 检查验证码
        String codeInCache = (String) redisTemplate.opsForValue().get(KeyProperties.VERIFICATION_CODE_PREFIX + phone);
        if (codeInCache == null) {
            return Response.error(ErrorCode.VERIFY_EXPIRE.getErrCode(), "验证码已过期");
        }
        if (!code.equals(codeInCache)) {
            return Response.error(ErrorCode.VERIFY_ERROR.getErrCode(), "验证码错误");
        }
        // 使用一次后就删除验证码
        redisTemplate.delete(KeyProperties.VERIFICATION_CODE_PREFIX + phone);

        // 查询角色
        LambdaQueryWrapper<UserRole> queryWrapperUserRole = new LambdaQueryWrapper<>();
        queryWrapperUserRole.eq(UserRole::getUserId, user.getId())
                .select(UserRole::getId, UserRole::getRoleId);
        List<UserRole> userRoles = userRoleMapper.selectList(queryWrapperUserRole);
        List<String> identities = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            String RoleId = userRole.getRoleId();
            LambdaQueryWrapper<Role> queryWrapperRole = new LambdaQueryWrapper<>();
            queryWrapperRole.eq(Role::getId, RoleId).select(Role::getId, Role::getRoleNameEn, Role::getRoleNameCn);
            Role role = roleMapper.selectOne(queryWrapperRole);
            identities.add(role.getRoleNameEn());
        }

        Response userInfo = getBaseInfo(user.getId());
        UserGetBaseInfoResponse userGetBaseInfoResponse = new UserGetBaseInfoResponse();
        if (userInfo.getSuccess()) {
            userGetBaseInfoResponse = (UserGetBaseInfoResponse) userInfo.getData();
        } else {
            return userInfo;
        }

        // 生成jwt
        UserTokenResponse userTokenResponse = generateToken(user.getId(), identities, userGetBaseInfoResponse);
        return Response.of(userTokenResponse);
    }

    @Override
    public Response loginWithPassword(UserLoginWithPasswordRequest userLoginWithPasswordRequest) {
        String phone = userLoginWithPasswordRequest.getPhone();
        String password = userLoginWithPasswordRequest.getPassword();

        // 根据手机号获取id和密码
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone).select(User::getPassword, User::getId);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "手机号未注册");
        }

        // 检查密码
        String encodedPassword = user.getPassword();
        boolean matches = passwordEncoder.matches(password, encodedPassword);
        if (!matches) {
            return Response.error(ErrorCode.VERIFY_ERROR.getErrCode(), "密码错误");
        }

        // 查询角色
        LambdaQueryWrapper<UserRole> queryWrapperUserRole = new LambdaQueryWrapper<>();
        queryWrapperUserRole.eq(UserRole::getUserId, user.getId())
                .select(UserRole::getId, UserRole::getRoleId);
        List<UserRole> userRoles = userRoleMapper.selectList(queryWrapperUserRole);
        List<String> identities = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            String RoleId = userRole.getRoleId();
            LambdaQueryWrapper<Role> queryWrapperRole = new LambdaQueryWrapper<>();
            queryWrapperRole.eq(Role::getId, RoleId).select(Role::getId, Role::getRoleNameEn, Role::getRoleNameCn);
            Role role = roleMapper.selectOne(queryWrapperRole);
            identities.add(role.getRoleNameEn());
        }

        Response userInfo = getBaseInfo(user.getId());
        UserGetBaseInfoResponse userGetBaseInfoResponse = new UserGetBaseInfoResponse();
        if (userInfo.getSuccess()) {
            userGetBaseInfoResponse = (UserGetBaseInfoResponse) userInfo.getData();
        } else {
            return userInfo;
        }

        // 生成jwt
        UserTokenResponse userTokenResponse = generateToken(user.getId(), identities, userGetBaseInfoResponse);
        return Response.of(userTokenResponse);
    }

    @Override
    public Response register(UserRegisterRequest userRegisterRequest) {
        String phone = userRegisterRequest.getPhone();
        String password = userRegisterRequest.getPassword();
        String code = userRegisterRequest.getCode();
        String encodedPassword = passwordEncoder.encode(password);

        // 判断是否注册过
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone).select(User::getId);
        User userPhone = userMapper.selectOne(queryWrapper);
        if (userPhone != null) {
            return Response.error(ErrorCode.DATA_EXIST.getErrCode(), "该手机号已经注册过");
        }

        // 判断验证码是否正确
        String codeInCache = (String) redisTemplate.opsForValue().get(KeyProperties.VERIFICATION_CODE_PREFIX + phone);
        if (codeInCache == null) {
            return Response.error(ErrorCode.VERIFY_EXPIRE.getErrCode(), "验证码已过期");
        }
        if (!code.equals(codeInCache)) {
            return Response.error(ErrorCode.VERIFY_ERROR.getErrCode(), "验证码错误");
        }
        // 使用一次后就删除验证码
        redisTemplate.delete(KeyProperties.VERIFICATION_CODE_PREFIX + phone);

        // 添加用户
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPassword(encodedPassword);
        user.setPhone(phone);
        userMapper.insert(user);

        // 添加普通用户身份
        UserRole userRole = new UserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setUserId(user.getId());
        userRole.setRoleId("1");
        userRoleMapper.insert(userRole);

        Response userInfo = getBaseInfo(user.getId());
        UserGetBaseInfoResponse userGetBaseInfoResponse = new UserGetBaseInfoResponse();
        if (userInfo.getSuccess()) {
            userGetBaseInfoResponse = (UserGetBaseInfoResponse) userInfo.getData();
        } else {
            return userInfo;
        }

        // 生成jwt
        UserTokenResponse userTokenResponse = generateToken(user.getId(), List.of("user"), userGetBaseInfoResponse);
        return Response.of(userTokenResponse);
    }

    @Override
    public Response refreshToken(String refreshToken) {
        Claims claims = null;
        try {
            claims = JwtUtil.parseJWT(refreshToken);
        } catch (ExpiredJwtException e) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "refreshToken已过期");
        } catch (Exception e) {
            return Response.error(ErrorCode.TOKEN_PARSE_ERROR.getErrCode(), "refreshToken解析失败");
        }
        String id = claims.getSubject();

        // 查询身份
        LambdaQueryWrapper<UserRole> queryWrapperUserRole = new LambdaQueryWrapper<>();
        queryWrapperUserRole.eq(UserRole::getUserId, id)
                .select(UserRole::getId, UserRole::getRoleId);
        List<UserRole> userRoles = userRoleMapper.selectList(queryWrapperUserRole);
        List<String> identities = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            String RoleId = userRole.getRoleId();
            LambdaQueryWrapper<Role> queryWrapperRole = new LambdaQueryWrapper<>();
            queryWrapperRole.eq(Role::getId, RoleId).select(Role::getId, Role::getRoleNameEn, Role::getRoleNameCn);
            Role role = roleMapper.selectOne(queryWrapperRole);
            identities.add(role.getRoleNameEn());
        }

        Response userInfo = getBaseInfo(id);
        UserGetBaseInfoResponse userGetBaseInfoResponse = new UserGetBaseInfoResponse();
        if (userInfo.getSuccess()) {
            userGetBaseInfoResponse = (UserGetBaseInfoResponse) userInfo.getData();
        } else {
            return userInfo;
        }

        UserTokenResponse userTokenResponse = generateToken(id, identities, userGetBaseInfoResponse);
        return Response.of(userTokenResponse);
    }

    @Override
    public Response getCode(String phone) {
        String preCode = (String) redisTemplate.opsForValue().get(KeyProperties.VERIFICATION_CODE_PREFIX + phone);
        if (preCode != null) {
            return Response.error(ErrorCode.REQUEST_FREQUENTLY.getErrCode(), "请求验证码过于频繁");
        }
        String code = CodeUtil.generateRandomCode(6);
        redisTemplate.opsForValue().set(KeyProperties.VERIFICATION_CODE_PREFIX + phone, code, 60, TimeUnit.SECONDS);
        SmsUtil.sendMessageToPhone(phone, "【CCPC】您的验证码为：" + code + "\n请勿告诉别人。");
        return Response.success();
    }

    @Override
    public Response updateBaseInfo(UserUpdateBaseInfoRequest userUpdateBaseInfoRequest) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 更新自己的信息
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getEmail, userUpdateBaseInfoRequest.getEmail())
                .set(User::getFirstName, userUpdateBaseInfoRequest.getFirstName())
                .set(User::getLastName, userUpdateBaseInfoRequest.getLastName())
                .set(User::getFirstNameEn, userUpdateBaseInfoRequest.getFirstNameEn())
                .set(User::getLastNameEn, userUpdateBaseInfoRequest.getLastNameEn())
                .set(User::getSex, userUpdateBaseInfoRequest.getSex())
                .set(User::getClothSize, userUpdateBaseInfoRequest.getClothSize())
                .set(User::getStudentNumber, userUpdateBaseInfoRequest.getStudentNumber())
                .set(User::getEnrollmentYear, userUpdateBaseInfoRequest.getEnrollmentYear())
                .set(User::getDegree, userUpdateBaseInfoRequest.getDegree())
                .set(User::getCollege, userUpdateBaseInfoRequest.getCollege());
        int result = userMapper.update(updateWrapper);
        if (result == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.UPDATE_FAILED);
        }
    }

    @Override
    public Response getBaseInfo() {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();
        return getBaseInfo(id);
    }

    private Response getBaseInfo(String id) {
        // 查询自己的信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).select(User::getId, User::getPhone, User::getEmail, User::getFirstName,
                User::getLastName, User::getFirstNameEn, User::getLastNameEn, User::getSex, User::getClothSize,
                User::getStudentNumber, User::getEnrollmentYear, User::getDegree, User::getCollege, User::getSchoolId);
        User user = userMapper.selectOne(queryWrapper);

        if (user != null) {
            // 查询学校名称
            String schoolName = "";
            if (user.getSchoolId() != null && !user.getSchoolId().isEmpty()) {
                LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
                queryWrapperSchool.eq(School::getId, user.getSchoolId()).select(School::getId, School::getName);
                School school = schoolMapper.selectOne(queryWrapperSchool);
                schoolName = school.getName();
            }

            UserGetBaseInfoResponse userGetBaseInfoResponse = new UserGetBaseInfoResponse();
            BeanUtils.copyProperties(user, userGetBaseInfoResponse);
            userGetBaseInfoResponse.setSchool(schoolName);
            return Response.of(userGetBaseInfoResponse);
        } else {
            return Response.error(ErrorCode.QUERY_FAILED);
        }
    }

    @Override
    public Response updateAddressInfo(UserUpdateAddressInfoRequest userUpdateAddressInfoRequest) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 更新自己的信息
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getAddress, userUpdateAddressInfoRequest.getAddress())
                .set(User::getAddressee, userUpdateAddressInfoRequest.getAddressee())
                .set(User::getAddressPhone, userUpdateAddressInfoRequest.getPhone());
        int result = userMapper.update(updateWrapper);
        if (result == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.UPDATE_FAILED);
        }
    }

    @Override
    public Response getAddressInfo() {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 查询自己的信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).select(User::getId, User::getAddress, User::getAddressee, User::getAddressPhone);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            UserGetAddressInfoResponse userGetAddressInfoResponse = new UserGetAddressInfoResponse();
            BeanUtils.copyProperties(user, userGetAddressInfoResponse);
            return Response.of(userGetAddressInfoResponse);
        } else {
            return Response.error(ErrorCode.QUERY_FAILED);
        }
    }

    @Override
    public Response updatePasswordInfo(UserUpdatePasswordInfoRequest userUpdatePasswordInfoRequest) {
        String code = userUpdatePasswordInfoRequest.getCode();
        String phone = userUpdatePasswordInfoRequest.getPhone();

        // 判断验证码是否正确
        String codeInCache = (String) redisTemplate.opsForValue().get(KeyProperties.VERIFICATION_CODE_PREFIX + phone);
        if (codeInCache == null) {
            return Response.error(ErrorCode.VERIFY_EXPIRE.getErrCode(), "验证码已过期");
        }
        if (!code.equals(codeInCache)) {
            return Response.error(ErrorCode.VERIFY_ERROR.getErrCode(), "验证码错误");
        }
        // 使用一次后就删除验证码
        redisTemplate.delete(KeyProperties.VERIFICATION_CODE_PREFIX + phone);

        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 加密密码
        String encodedPassword = passwordEncoder.encode(userUpdatePasswordInfoRequest.getPassword());

        // 更新自己的信息
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getPassword, encodedPassword);
        int result = userMapper.update(updateWrapper);
        if (result == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.UPDATE_FAILED);
        }
    }

    @Override
    public Response getPasswordInfo() {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 查询自己的信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).select(User::getId, User::getPhone);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            UserGetPasswordInfoResponse userGetPasswordInfoResponse = new UserGetPasswordInfoResponse();
            BeanUtils.copyProperties(user, userGetPasswordInfoResponse);
            return Response.of(userGetPasswordInfoResponse);
        } else {
            return Response.error(ErrorCode.QUERY_FAILED);
        }
    }

    public Response getAllCoachStudent(StudentQueryRequest studentQueryRequest) {

        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();
        Integer pageNow = studentQueryRequest.getPageNow();
        Integer pageSize = studentQueryRequest.getPageSize();

        Page<User> page = new Page<>(pageNow, pageSize);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getCoachId, id)
                .select(User::getId, User::getFirstName, User::getLastName, User::getFirstNameEn,
                        User::getLastNameEn, User::getPhone, User::getEmail, User::getSex, User::getDegree,
                        User::getCollege, User::getStudentNumber, User::getEnrollmentYear, User::getCoachId);
        //进行模糊查询
        queryWrapper = queryStudent(studentQueryRequest, queryWrapper);

        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        List<User> users = userPage.getRecords();
        List<UserGetAllStudentResponse> userGetAllStudentResponses = new ArrayList<>();
        for (User user : users) {
            UserGetAllStudentResponse userGetAllStudentResponse = new UserGetAllStudentResponse();
            BeanUtils.copyProperties(user, userGetAllStudentResponse);
            userGetAllStudentResponses.add(userGetAllStudentResponse);
        }
        return Response.of(userGetAllStudentResponses);
    }

    //对学生的条件查询
    private LambdaQueryWrapper<User> queryStudent(StudentQueryRequest studentQueryRequest, LambdaQueryWrapper<User> queryWrapper) {

        // 按姓名模糊查询
        if (studentQueryRequest.getName() != null) {
            String name = studentQueryRequest.getName();
            if (StringUtils.hasText(name)) {
                String safeName = "%" + name + "%";
                queryWrapper.apply("CONCAT(first_name, last_name) LIKE {0}", safeName);
            }
        }


        // 按学号模糊
        if (studentQueryRequest.getStudentNumber() != null) {
            String studentNumber = studentQueryRequest.getStudentNumber();
            if (StringUtils.hasText(studentNumber)) {
                queryWrapper.like(User::getStudentNumber, studentNumber);
            }
        }


        // 按手机号模糊查询

        if (studentQueryRequest.getPhone() != null) {
            String phone = studentQueryRequest.getPhone();
            queryWrapper.like(User::getPhone, phone);
        }


        // 按学院模糊查询

        if (studentQueryRequest.getCollege() != null) {
            String college = studentQueryRequest.getCollege();

            if (StringUtils.hasText(college)) {
                queryWrapper.like(User::getCollege, college);
            }
        }


        // 按入学年份匹配
        if (studentQueryRequest.getEnrollmentYear() != null) {
            String enrollmentYear = studentQueryRequest.getEnrollmentYear();

            if (StringUtils.hasText(enrollmentYear)) {
                queryWrapper.eq(User::getEnrollmentYear, enrollmentYear);
            }
        }


        // 按性别匹配
        if (studentQueryRequest.getSex() != null) {
            String sex = studentQueryRequest.getSex();
            if (StringUtils.hasText(sex)) {
                boolean sex1;
                if (sex.equals("true") || sex.equals("1")) {
                    sex1 = true;
                } else {
                    sex1 = false;
                }
                queryWrapper.eq(User::getSex, sex1);
            }
        }

        //排序字段处理
        if (studentQueryRequest.getSortType() != null) {
            String sortType = studentQueryRequest.getSortType();

            if (StringUtils.hasText(sortType)) {
                String[] parts = sortType.split(":");
                String field = parts[0];
                String direction = parts.length > 1 ? parts[1] : "asc";

                switch (field) {
                    case "name":
                        addOrder(queryWrapper, User::getFirstName, direction);
                        break;
                    case "studentNumber":
                        addOrder(queryWrapper, User::getStudentNumber, direction);
                        break;
                    case "college":
                        addOrder(queryWrapper, User::getCollege, direction);
                        break;
                    default:
                        queryWrapper.orderByAsc(User::getId);
                }
            }
        }

        return queryWrapper;
    }

    // 辅助方法 排序字段的处理
    private void addOrder(LambdaQueryWrapper<User> wrapper, SFunction<User, ?> field, String direction) {
        if ("desc".equalsIgnoreCase(direction)) {
            wrapper.orderByDesc(field);
        } else {
            wrapper.orderByAsc(field);
        }
    }

    public Response getCoachStudentCount(StudentQueryRequest studentQueryRequest) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getCoachId, id);

        queryWrapper = queryStudent(studentQueryRequest, queryWrapper);

        Long count = userMapper.selectCount(queryWrapper);
        return Response.of(count);
    }


    @Override
    public Response getAllSchoolStudent(int pageNow, int pageSize, String sortType, String keyword) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 查询自己的学校
        LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
        queryWrapperUser.eq(User::getId, id).select(User::getId, User::getSchoolId);
        User userSchoolAdmin = userMapper.selectOne(queryWrapperUser);
        String schoolId = userSchoolAdmin.getSchoolId();

        // 查询所有的学生
        Page<User> page = new Page<>(pageNow, pageSize);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getSchoolId, schoolId)
                .select(User::getId, User::getFirstName, User::getLastName, User::getFirstNameEn,
                        User::getLastNameEn, User::getPhone, User::getEmail, User::getSex, User::getDegree,
                        User::getCollege, User::getStudentNumber, User::getEnrollmentYear, User::getCoachId);
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(User::getFirstName, keyword); // name 字段包含 keyword
            queryWrapper.like(User::getLastName, keyword);
        }
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        List<User> users = userPage.getRecords();
        List<UserGetAllStudentResponse> userGetAllStudentRespons = new ArrayList<>();
        for (User user : users) {
            UserGetAllStudentResponse userGetAllStudentResponse = new UserGetAllStudentResponse();
            BeanUtils.copyProperties(user, userGetAllStudentResponse);
            userGetAllStudentRespons.add(userGetAllStudentResponse);
        }
        return Response.of(userGetAllStudentRespons);
    }

    @Override
    public Response getSchoolStudentCount(String keyword) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 查询自己的学校
        LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
        queryWrapperUser.eq(User::getId, id).select(User::getId, User::getSchoolId);
        User user = userMapper.selectOne(queryWrapperUser);
        String schoolId = user.getSchoolId();

        // 查询所有的学生
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getSchoolId, schoolId);
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(User::getFirstName, keyword); // name 字段包含 keyword
            queryWrapper.like(User::getLastName, keyword);
        }
        Long count = userMapper.selectCount(queryWrapper);
        return Response.of(count);
    }

    @Override
    public Response generateCoachInviteToken(Long seconds) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 先作废旧的邀请链接
        String preCoachInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.COACH_INVITE_TOKEN_PREFIX + id);
        if (preCoachInviteToken != null) {
            redisTemplate.delete(KeyProperties.COACH_INVITE_TOKEN_PREFIX + id);
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).select(User::getId, User::getSchoolId);
        User user = userMapper.selectOne(queryWrapper);

        Map<String, String> map = new HashMap<>();
        map.put("type", "coachInviteToken");
        map.put("schoolId", user.getSchoolId());
        map.put("coachId", id);
        map.put("createUserId", id);
        String coachInviteToken = JwtUtil.generateToken(JSON.toJSONString(map), seconds * 1000L);
        redisTemplate.opsForValue().set(KeyProperties.COACH_INVITE_TOKEN_PREFIX + id, coachInviteToken, seconds, TimeUnit.SECONDS);
        return Response.of(coachInviteToken);
    }

    @Override
    public Response acceptCoachInvite(String token) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 解析token中的数据
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (ExpiredJwtException e) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "教练邀请链接已过期");
        } catch (Exception e) {
            return Response.error(ErrorCode.TOKEN_PARSE_ERROR.getErrCode(), "教练邀请链接解析失败");
        }
        Map<String, String> map = JSON.parseObject(claims.getSubject(), new TypeReference<Map<String, String>>() {
        });
        String type = map.get("type");
        String schoolId = map.get("schoolId");
        String coachId = map.get("coachId");
        String createUserId = map.get("createUserId");
        if (type == null || schoolId == null || coachId == null || createUserId == null) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
        }

        if (!type.equals("coachInviteToken")) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "该token无法用作加入教练麾下");
        }

        // 看是否失效
        String preCoachInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.COACH_INVITE_TOKEN_PREFIX + createUserId);
        if (preCoachInviteToken == null || !preCoachInviteToken.equals(token)) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "token已失效");
        }

        // 加入教练和学校
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getSchoolId, schoolId)
                .set(User::getCoachId, coachId);
        int result = userMapper.update(updateWrapper);
        if (result == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "加入教练麾下失败");
        }
    }

    @Override
    public Response parseInviteToken(String token) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 解析token中的数据
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (ExpiredJwtException e) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "token已过期");
        } catch (Exception e) {
            return Response.error(ErrorCode.TOKEN_PARSE_ERROR.getErrCode(), "token解析失败");
        }
        Map<String, String> map = JSON.parseObject(claims.getSubject(), new TypeReference<Map<String, String>>() {
        });

        // 判断token类型
        String type = map.get("type");
        if (type == null) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
        }

        // 返回相应信息
        switch (type) {
            case "coachInviteToken" -> {
                String schoolId = map.get("schoolId");
                String coachId = map.get("coachId");
                String createUserId = map.get("createUserId");
                if (schoolId == null || coachId == null || createUserId == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
                }

                // 看是否失效
                String preCoachInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.COACH_INVITE_TOKEN_PREFIX + createUserId);
                if (preCoachInviteToken == null || !preCoachInviteToken.equals(token)) {
                    return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "token已失效");
                }

                // 查询教练信息
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getId, coachId)
                        .select(User::getId, User::getFirstName, User::getLastName, User::getFirstNameEn, User::getLastNameEn);
                User user = userMapper.selectOne(queryWrapper);
                if (user == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "教练不存在");
                }

                // 查询学校信息
                LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
                queryWrapperSchool.eq(School::getId, schoolId)
                        .select(School::getId, School::getName, School::getNameEn);
                School school = schoolMapper.selectOne(queryWrapperSchool);
                if (school == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "学校不存在");
                }

                Map<String, String> ans = new HashMap<>();
                ans.put("type", "coachInviteToken");
                ans.put("schoolName", school.getName());
                ans.put("schoolNameEn", school.getNameEn());
                ans.put("coachName", user.getFirstName() + user.getLastName());
                ans.put("coachNameEn", user.getFirstNameEn() + user.getLastNameEn());
                return Response.of(ans);
            }
            case "coachTeamInviteToken" -> {
                String schoolId = map.get("schoolId");
                String coachId = map.get("coachId");
                String createUserId = map.get("createUserId");
                if (schoolId == null || coachId == null || createUserId == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
                }

                // 看是否失效
                String preCoachTeamInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.COACH_TEAM_INVITE_TOKEN_PREFIX + createUserId);
                if (preCoachTeamInviteToken == null || !preCoachTeamInviteToken.equals(token)) {
                    return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "token已失效");
                }

                // 查询教练信息
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getId, coachId)
                        .select(User::getId, User::getFirstName, User::getLastName, User::getFirstNameEn, User::getLastNameEn);
                User user = userMapper.selectOne(queryWrapper);
                if (user == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "教练不存在");
                }

                // 查询学校信息
                LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
                queryWrapperSchool.eq(School::getId, schoolId)
                        .select(School::getId, School::getName, School::getNameEn);
                School school = schoolMapper.selectOne(queryWrapperSchool);
                if (school == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "学校不存在");
                }

                Map<String, String> ans = new HashMap<>();
                ans.put("type", "coachTeamInviteToken");
                ans.put("schoolName", school.getName());
                ans.put("schoolNameEn", school.getNameEn());
                ans.put("coachName", user.getFirstName() + user.getLastName());
                ans.put("coachNameEn", user.getFirstNameEn() + user.getLastNameEn());
                return Response.of(ans);
            }
            case "teamInviteToken" -> {
                String teamId = map.get("teamId");
                if (teamId == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
                }

                // 看是否失效
                String preTeamInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.TEAM_INVITE_TOKEN_PREFIX + teamId);
                if (preTeamInviteToken == null || !preTeamInviteToken.equals(token)) {
                    return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "token已失效");
                }

                // 查询队伍信息
                LambdaQueryWrapper<Team> queryWrapperTeam = new LambdaQueryWrapper<>();
                queryWrapperTeam.eq(Team::getId, teamId)
                        .select(Team::getId, Team::getName, Team::getNameEn);
                Team team = teamMapper.selectOne(queryWrapperTeam);
                if (team == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "队伍不存在");
                }

                Map<String, String> ans = new HashMap<>();
                ans.put("type", "teamInviteToken");
                ans.put("teamName", team.getName());
                ans.put("teamNameEn", team.getNameEn());
                return Response.of(ans);
            }
            case "schoolInviteToken" -> {
                String schoolId = map.get("schoolId");
                String createUserId = map.get("createUserId");
                if (schoolId == null || createUserId == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
                }

                // 先看是否失效
                String preSchoolInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.SCHOOL_INVITE_TOKEN_PREFIX + createUserId);
                if (preSchoolInviteToken == null || !preSchoolInviteToken.equals(token)) {
                    return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "token已失效");
                }

                // 查询学校信息
                LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
                queryWrapperSchool.eq(School::getId, schoolId)
                        .select(School::getId, School::getName, School::getNameEn);
                School school = schoolMapper.selectOne(queryWrapperSchool);
                if (school == null) {
                    return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "学校不存在");
                }

                Map<String, String> ans = new HashMap<>();
                ans.put("type", "schoolInviteToken");
                ans.put("schoolName", school.getName());
                ans.put("schoolNameEn", school.getNameEn());
                return Response.of(ans);
            }
            default -> {
                return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
            }
        }
    }

    @Override
    public Response getAllInviteToken() {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 从redis中获取token
        String coachInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.COACH_INVITE_TOKEN_PREFIX + id);
        Long coachInviteExpire = redisTemplate.getExpire(KeyProperties.COACH_INVITE_TOKEN_PREFIX + id, TimeUnit.SECONDS);

        String coachTeamInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.COACH_TEAM_INVITE_TOKEN_PREFIX + id);
        Long coachTeamInviteExpire = redisTemplate.getExpire(KeyProperties.COACH_TEAM_INVITE_TOKEN_PREFIX + id, TimeUnit.SECONDS);

        String schoolInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.SCHOOL_INVITE_TOKEN_PREFIX + id);
        Long schoolInviteExpire = redisTemplate.getExpire(KeyProperties.SCHOOL_INVITE_TOKEN_PREFIX + id, TimeUnit.SECONDS);

        UserGetAllInviteTokenResponse userGetAllInviteTokenResponse = new UserGetAllInviteTokenResponse();
        userGetAllInviteTokenResponse.setCoachInviteToken(coachInviteToken);
        userGetAllInviteTokenResponse.setCoachInviteExpire(coachInviteExpire);
        userGetAllInviteTokenResponse.setCoachTeamInviteToken(coachTeamInviteToken);
        userGetAllInviteTokenResponse.setCoachTeamInviteExpire(coachTeamInviteExpire);
        userGetAllInviteTokenResponse.setSchoolInviteToken(schoolInviteToken);
        userGetAllInviteTokenResponse.setSchoolInviteExpire(schoolInviteExpire);
        return Response.of(userGetAllInviteTokenResponse);
    }

    @Override
    public Response getCoachStudent(String id) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id)
                .select(
                        // 基础信息字段
                        User::getId, User::getPhone, User::getEmail, User::getFirstName,
                        User::getLastName, User::getFirstNameEn, User::getLastNameEn,
                        User::getSex, User::getClothSize, User::getStudentNumber,
                        User::getEnrollmentYear, User::getDegree, User::getCollege,
                        User::getSchoolId,
                        // 地址信息字段
                        User::getAddress, User::getAddressee, User::getAddressPhone
                );

        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            return Response.error(ErrorCode.QUERY_FAILED);
        }

        CoachUpdateStudentResponse response = new CoachUpdateStudentResponse();
        BeanUtils.copyProperties(user, response);
        return Response.of(response);
    }

    @Override
    public Response updateCoachStudent(CoachUpdateStudentRequest coachUpdateStudentRequest) {
    //获取学生id
    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(User::getPhone, coachUpdateStudentRequest.getPhone())
            .select(User::getId);
    User user = userMapper.selectOne(queryWrapper);
    String id = user.getId();
    // 更新信息
    LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(User::getId, id)
            .set(User::getEmail, coachUpdateStudentRequest.getEmail())
            .set(User::getFirstName, coachUpdateStudentRequest.getFirstName())
            .set(User::getLastName, coachUpdateStudentRequest.getLastName())
            .set(User::getFirstNameEn, coachUpdateStudentRequest.getFirstNameEn())
            .set(User::getLastNameEn, coachUpdateStudentRequest.getLastNameEn())
            .set(User::getSex, coachUpdateStudentRequest.getSex())
            .set(User::getStudentNumber, coachUpdateStudentRequest.getStudentNumber())
            .set(User::getClothSize, coachUpdateStudentRequest.getClothSize())
            .set(User::getEnrollmentYear, coachUpdateStudentRequest.getEnrollmentYear())
            .set(User::getDegree, coachUpdateStudentRequest.getDegree())
            .set(User::getCollege, coachUpdateStudentRequest.getCollege())
            .set(User::getAddress,coachUpdateStudentRequest.getAddress())
            .set(User::getAddressee, coachUpdateStudentRequest.getAddressee())
            .set(User::getAddressPhone, coachUpdateStudentRequest.getAddressPhone());
    int result = userMapper.update(updateWrapper);
    if (result == 1) {
        return Response.success();
    } else {
        return Response.error(ErrorCode.UPDATE_FAILED);
    }
}


}

