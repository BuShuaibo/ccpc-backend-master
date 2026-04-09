package com.neuq.ccpcbackend.controller;

import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.service.SignupService;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/signup")
public class SignupController {
    @Resource
    private SignupService signupService;

    @GetMapping(("/getAllCompetitionAdminSignupChangeRequest"))
    public Response getAllCompetitionAdminSignupChangeRequest(@RequestParam int pageNow, @RequestParam int pageSize, @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status)
    {
        return signupService.getAllCompetitionAdminSignupChangeRequest(pageNow, pageSize, keyword, status);
    }

    @PostMapping("/auditSignupChangeRequest")
    public Response auditSignupChangeRequest(@RequestBody AdminAuditSignupChangeRequest request)
    {
        return signupService.auditSignupChangeRequest(request);
    }

    @GetMapping("/adminGetAllSchoolName")
    public Response adminGetAllSchoolName()
    {
        return signupService.getAllSchoolName();
    }


    @GetMapping("/getCompetitionAdminSignupInfo")
    public Response getCompetitionAdminSignupInfo(@RequestParam int pageNow, @RequestParam int pageSize, @RequestParam(required = false) Integer status)
    {
        return signupService.getAllCompetitionAdminSignupInfo(pageNow, pageSize, status);
    }



    @PostMapping("/auditSignup")
    public Response auditSignup(@RequestParam String signupId)
    {
        return signupService.updateSignupStatus(signupId, 1);
    }

    @PostMapping("/auditPaymentReceipt")
    public Response auditPaymentReceipt(@RequestParam String signupId)
    {
        return signupService.updateSignupStatus(signupId, 2);
    }


    @GetMapping("/getTeamByCoachId")
    public Response getTeamByCoachId(@RequestParam String coachId, @RequestParam int pageNow, @RequestParam int pageSize,
                                     @RequestParam String season){
        return signupService.getTeamByCoachId(coachId,pageNow, pageSize, season);
    }
    @PostMapping("/updateTeamNameByCoachId")
    public Response updateTeamNameByCoachId(@Valid @RequestBody SignupUpdateTeamNameRequest signupUpdateTeamNameRequest) {
        return signupService.updateTeamNameByCoachId(signupUpdateTeamNameRequest);
    }

    @GetMapping("/getSignupInfoById")
    public Response getSignupInfoById(@RequestParam String signupId) {
        return signupService.getSignupInfoById(signupId);
    }

    @GetMapping("/checkSignupChanging")
    public Response checkSignupChanging(@RequestParam String signupId) {
        return signupService.checkSignupChanging(signupId);
    }

    @GetMapping("/getSchoolCoach")
    public Response getSchoolCoach() {
        return signupService.getSchoolCoach();
    }

    @GetMapping("/getCoachStudent")
    public Response getCoachStudent() {
        return signupService.getCoachStudent();
    }

    @GetMapping("/getSignupRequestChangeBySignupId")
    public Response getSignupRequestChangeBySignupId(@RequestParam String signupId) {
        return signupService.getSignupRequestChangeBySignupId(signupId);
    }

    @GetMapping("/getSchoolCompetitionQuotaRest")
    public Response getSchoolCompetitionQuotaRest(@RequestParam String competitionId) {
        return signupService.getSchoolCompetitionQuotaRest(competitionId);
    }

    @PostMapping("/updateSignupInfo")
    public Response updateSignupInfo(@Valid @RequestBody SignupUpdateInfoRequest request) {
        return signupService.updateSignupInfo(request);
    }

    @GetMapping("/getSignupByCoachId")
    public Response getSignupByCoachId(@RequestParam int pageNow,@RequestParam int pageSize,@RequestParam(required = false) String competition,@RequestParam(required = false) String teamMembers,@RequestParam(required = false) String teamName,@RequestParam(required = false) Integer processStatus,@RequestParam(required = false) Integer occupationQuotaType,@RequestParam(required = false) String prizeCoach)
    {
        return signupService.getSignupByCoachId(pageNow, pageSize, competition, teamMembers, teamName, processStatus, occupationQuotaType, prizeCoach);
    }

    @PostMapping("/signupCompetition")
    public Response signupCompetition(@RequestParam String competitionId, @Valid @RequestBody List<SignupCompetitionRequest> requests) {
        return signupService.signupCompetitions(competitionId, requests);
    }

    @PostMapping("/uploadPayment")
    public Response uploadPayment(@RequestParam("file") MultipartFile file, @RequestParam String SignupId)
    {
        return signupService.uploadPayment(file,SignupId);
    }
}
