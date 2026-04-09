package com.neuq.ccpcbackend.service;

import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.utils.response.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SignupService {
    Response getAllCompetitionAdminSignupChangeRequest(int pageNow,int pageSize,String keyword,Integer status);

    Response auditSignupChangeRequest(AdminAuditSignupChangeRequest request);

    Response getAllCompetitionAdminSignupInfo(int pageNow, int pageSize, Integer status);

    Response updateSignupStatus(String signupId, Integer status);

    Response getTeamByCoachId(String coachId, int pageNow, int pageSize, String season);

    Response updateTeamNameByCoachId(SignupUpdateTeamNameRequest signupUpdateTeamNameRequest);

    Response updateSignupInfo(SignupUpdateInfoRequest request);

    Response getSignupInfoById(String signupId);

    Response checkSignupChanging(String signupId);

    Response getSchoolCoach();

    Response getCoachStudent();

    Response getSignupRequestChangeBySignupId(String signupId);

    Response getSchoolCompetitionQuotaRest(String competitionId);

    Response getSignupByCoachId(int pageNow,int pageSize, String competition, String teamMembers, String teamName, Integer processStatus, Integer occupationQuotaType,String prizeCoach);

    Response signupCompetitions(String competitionId, List<SignupCompetitionRequest> requests);

    Response getAllSchoolName();

    Response uploadPayment(MultipartFile file, String signupId);
}
