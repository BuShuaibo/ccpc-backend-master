package com.neuq.ccpcbackend.controller;

import com.neuq.ccpcbackend.dto.AdminUpdateQuotationRequest;
import com.neuq.ccpcbackend.dto.CompetitionUpdateInfoRequest;
import com.neuq.ccpcbackend.service.CompetitionService;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Transactional
@RestController
@RequestMapping("/api/competition")
public class CompetitionController {
    @Resource
    private CompetitionService competitionService;
    @GetMapping("/exportCompetingSchools")
    public Response exportSchools(@RequestParam String competitionId,HttpServletResponse response) throws IOException {
        return competitionService.exportCompetingSchools(competitionId,response);
    }
    @GetMapping("/exportSignupMembers")
    public void exportSignupMembers(@RequestParam String competitionId,HttpServletResponse response) throws IOException {
        competitionService.exportTeamMembers(competitionId,response);
    }
    @GetMapping("/downloadImportTemplate")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        competitionService.downloadImportTemplate(response);
    }
    @PostMapping("/upload")
    public Response uploadExcel(@RequestParam("file") MultipartFile file)
    {
        return competitionService.uploadExcel(file);
    }

    @PostMapping("/AdminUpdateQuotationRequest")
    public Response updateQuotationRequest(@RequestBody AdminUpdateQuotationRequest request)
    {
        return competitionService.updateQuotationRequest(request);
    }
    @GetMapping("/exportTeam")
    public Response exportTeams(@RequestParam String competitionId,HttpServletResponse response) throws IOException {
        return competitionService.exportTeam(competitionId,response);
    }

    @GetMapping("/exportCompetingSchoolsEmblem")
    public Response exportCompetingSchoolsEmblem(@RequestParam String competitionId,HttpServletResponse response) throws IOException {
        return competitionService.exportCompetingSchoolsEmblem(competitionId,response);
    }
    @GetMapping("/exportCoaches")
    public Response exportCoaches(@RequestParam String competitionId,HttpServletResponse response) throws IOException {
        return competitionService.exportCoaches(competitionId,response);
    }

    @GetMapping("/getAllCompetitionInfo")
    public Response getAllCompetition(@RequestParam int pageNow, @RequestParam int pageSize, @RequestParam(required = false) String keyword, @RequestParam(required = false) String type, @RequestParam String season) {return competitionService.getAllCompetition(pageNow,pageSize,keyword,type,season);}



    @PostMapping("/updateInfo")
    public Response updateCompetitionInfo(@RequestBody CompetitionUpdateInfoRequest request) {
        competitionService.updateCompetitionInfo(request);
        return Response.success();
    }
    //  getCompetitionInfo ch
// getCoachTeam ch
// getSchoolInvoiceInfo ch
// getSchoolCompetitionQuotaRest ch

    @GetMapping("/getCompetitionInfo")
    public Response getCompetitionInfo(@RequestParam String competitionId){
        return competitionService.getCompetitionInfo(competitionId);
    }

    @GetMapping("/getCoachTeam")
    public Response getCoachTeam(@RequestParam String competitionId){
        return competitionService.getCoachTeam(competitionId);
    }

    @GetMapping("/getSchoolInvoiceInfo")
    public Response getSchoolInvoiceInfo(@RequestParam String competitionId){
        return competitionService.getSchoolInvoiceInfo(competitionId);
    }

    @GetMapping("/getSchoolCompetitionQuotaRest")
    public Response getSchoolCompetitionQuotaRest(@RequestParam String competitionId){
        return competitionService.getSchoolCompetitionQuotaRest(competitionId);
    }

    @GetMapping("/exportClothSizeStats")
    public void exportClothSizeStats(@RequestParam String competitionId, HttpServletResponse response) throws IOException {
        competitionService.exportClothSizeStats(competitionId, response);
    }

    @GetMapping("/exportInvoiceInfo")
    public Response exportInvoiceInfo(@RequestParam String competitionId, HttpServletResponse response) {
        return competitionService.exportInvoiceInfo(competitionId, response);
    }
}
