package com.neuq.ccpcbackend.service;

import com.neuq.ccpcbackend.dto.SchoolUpdateInfoRequest;
import com.neuq.ccpcbackend.utils.response.Response;

public interface SchoolService {
    Response updateInfo(SchoolUpdateInfoRequest schoolUpdateInfoRequest);
    Response getInfo();
    Response generateSchoolInviteToken(Long seconds);
    Response acceptSchoolInvite(String token);
}
