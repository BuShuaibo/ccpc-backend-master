package com.neuq.ccpcbackend.service;

import com.neuq.ccpcbackend.dto.AdminUpdateQuotationRequest;
import com.neuq.ccpcbackend.dto.CompetitionUpdateInfoRequest;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;


public interface CompetitionService {
    void updateCompetitionInfo(CompetitionUpdateInfoRequest request);
    Response exportCompetingSchools(String competitionId, HttpServletResponse response);
    Response exportCompetingSchoolsEmblem(String competitionId,HttpServletResponse response);
    Response exportTeam(String competitionId,HttpServletResponse response);
    Response exportCoaches(String competitionId,HttpServletResponse response);
    Response exportTeamMembers(String competitionId,HttpServletResponse response);
    Response getAllCompetition(int pageNow,int pageSize,String keyword,String type,String season);

    void downloadImportTemplate(HttpServletResponse response);

    Response uploadExcel(MultipartFile file);

    Response updateQuotationRequest(AdminUpdateQuotationRequest request);

    Response getCompetitionInfo(String competitionId);

    Response getCoachTeam(String competitionId);

    Response getCoachStudent(String competitionId);

    Response getSchoolInvoiceInfo(String competitionId);

    Response getSchoolCompetitionQuotaRest(String competitionId);

    void exportClothSizeStats(String competitionId, HttpServletResponse response);

    Response exportInvoiceInfo(String competitionId, HttpServletResponse response);
}
