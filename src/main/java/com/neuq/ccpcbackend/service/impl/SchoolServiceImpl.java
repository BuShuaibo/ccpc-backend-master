package com.neuq.ccpcbackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.neuq.ccpcbackend.dto.SchoolGetInfoResponse;
import com.neuq.ccpcbackend.dto.SchoolUpdateInfoRequest;
import com.neuq.ccpcbackend.entity.School;
import com.neuq.ccpcbackend.entity.User;
import com.neuq.ccpcbackend.mapper.SchoolMapper;
import com.neuq.ccpcbackend.mapper.UserMapper;
import com.neuq.ccpcbackend.properties.KeyProperties;
import com.neuq.ccpcbackend.service.SchoolService;
import com.neuq.ccpcbackend.utils.JwtUtil;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.response.Response;
import com.neuq.ccpcbackend.utils.RoleUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SchoolServiceImpl implements SchoolService {

    @Resource
    UserMapper userMapper;

    @Resource
    SchoolMapper schoolMapper;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public Response updateInfo(SchoolUpdateInfoRequest schoolUpdateInfoRequest) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 查询自己学校的id
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).select(User::getId, User::getSchoolId);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "用户不存在");
        }
        String schoolId = user.getSchoolId();
        if (schoolId == null || schoolId.isEmpty()) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "您不属于任何学校");
        }

        // 更新信息
        LambdaUpdateWrapper<School> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(School::getId, schoolId)
                .set(School::getNameEn, schoolUpdateInfoRequest.getNameEn())
                .set(School::getMailingAddress, schoolUpdateInfoRequest.getMailingAddress())
                .set(School::getInstitution, schoolUpdateInfoRequest.getInstitution())
                .set(School::getTaxpayerCode, schoolUpdateInfoRequest.getTaxpayerCode())
                .set(School::getInvoiceAddress, schoolUpdateInfoRequest.getAddress())
                .set(School::getInvoicePhone, schoolUpdateInfoRequest.getPhone())
                .set(School::getBankName, schoolUpdateInfoRequest.getBankName())
                .set(School::getBankCardCode, schoolUpdateInfoRequest.getBankCardCode());
        int result = schoolMapper.update(updateWrapper);
        if (result == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.UPDATE_FAILED);
        }
    }

    @Override
    public Response getInfo() {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 查找自己学校id
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).select(User::getId, User::getSchoolId);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "用户不存在");
        }
        String schoolId = user.getSchoolId();
        if (schoolId == null || schoolId.isEmpty()) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "您不属于任何学校");
        }

        LambdaQueryWrapper<School> queryWrapperSchool = new LambdaQueryWrapper<>();
        queryWrapperSchool.eq(School::getId, schoolId)
                .select(School::getId, School::getName, School::getNameEn, School::getMailingAddress,
                        School::getInstitution, School::getTaxpayerCode, School::getInvoiceAddress,
                        School::getInvoicePhone, School::getBankName, School::getBankCardCode);
        School school = schoolMapper.selectOne(queryWrapperSchool);
        if (school == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "学校不存在");
        }
        SchoolGetInfoResponse schoolGetInfoResponse = new SchoolGetInfoResponse();
        BeanUtils.copyProperties(school, schoolGetInfoResponse);
        return Response.of(schoolGetInfoResponse);
    }

    @Override
    public Response generateSchoolInviteToken(Long seconds) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 先作废旧的邀请链接
        String preSchoolInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.SCHOOL_INVITE_TOKEN_PREFIX + id);
        if (preSchoolInviteToken != null) {
            redisTemplate.delete(KeyProperties.SCHOOL_INVITE_TOKEN_PREFIX + id);
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).select(User::getId, User::getSchoolId);
        User user = userMapper.selectOne(queryWrapper);
        Map<String, String> map = new HashMap<>();
        map.put("type", "schoolInviteToken");
        map.put("schoolId", user.getSchoolId());
        map.put("createUserId", id);
        String schoolInviteToken = JwtUtil.generateToken(JSON.toJSONString(map), seconds * 1000L);
        redisTemplate.opsForValue().set(KeyProperties.SCHOOL_INVITE_TOKEN_PREFIX + id, schoolInviteToken, seconds, TimeUnit.SECONDS);
        return Response.of(schoolInviteToken);
    }

    @Override
    public Response acceptSchoolInvite(String token) {
        // 从ss线程上下文获得用户id
        String id = RoleUtil.getCurrentUserId();

        // 解析token中的数据
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (ExpiredJwtException e) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "学校邀请链接已过期");
        } catch (Exception e) {
            return Response.error(ErrorCode.TOKEN_PARSE_ERROR.getErrCode(), "学校邀请链接解析失败");
        }
        Map<String, String> map = JSON.parseObject(claims.getSubject(), new TypeReference<Map<String, String>>() {});
        String type = map.get("type");
        String schoolId = map.get("schoolId");
        String createUserId = map.get("createUserId");
        if (type == null || schoolId == null || createUserId == null) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "token内容错误");
        }

        if (!type.equals("schoolInviteToken")) {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "该token无法用作加入学校");
        }

        // 先看是否失效
        String preSchoolInviteToken = (String) redisTemplate.opsForValue().get(KeyProperties.SCHOOL_INVITE_TOKEN_PREFIX + createUserId);
        if (preSchoolInviteToken == null || !preSchoolInviteToken.equals(token)) {
            return Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "token已失效");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getSchoolId, schoolId);
        int result = userMapper.update(updateWrapper);
        if (result == 1) {
            return Response.success();
        } else {
            return Response.error(ErrorCode.ACCEPT_INVITE_FAILED.getErrCode(), "加入学校失败");
        }
    }
}
