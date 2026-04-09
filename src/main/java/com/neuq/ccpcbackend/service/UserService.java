package com.neuq.ccpcbackend.service;

import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.utils.response.Response;


public interface UserService {
    Response loginWithCode(UserLoginWithCodeRequest userLoginWithCodeRequest);
    Response loginWithPassword(UserLoginWithPasswordRequest userLoginWithPasswordRequest);
    Response register(UserRegisterRequest userRegisterRequest);
    Response refreshToken(String refreshToken);
    Response getCode(String phone);
    Response updateBaseInfo(UserUpdateBaseInfoRequest userUpdateBaseInfoRequest);
    Response getBaseInfo();
    Response updateAddressInfo(UserUpdateAddressInfoRequest userUpdateAddressInfoRequest);
    Response getAddressInfo();
    Response updatePasswordInfo(UserUpdatePasswordInfoRequest userUpdatePasswordInfoRequest);
    Response getPasswordInfo();
    Response getAllCoachStudent(StudentQueryRequest studentQueryRequest);
    Response getCoachStudentCount(StudentQueryRequest studentQueryRequest);
    Response getAllSchoolStudent(int pageNow, int pageSize, String sortType, String keyword);
    Response getSchoolStudentCount(String keyword);
    Response generateCoachInviteToken(Long seconds);
    Response acceptCoachInvite(String token);
    Response parseInviteToken(String token);
    Response getAllInviteToken();
    Response getCoachStudent(String id);
    Response updateCoachStudent(CoachUpdateStudentRequest coachUpdateStudentRequest);
}
