package com.neuq.ccpcbackend.controller;

import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.entity.Sponsor;
import com.neuq.ccpcbackend.service.AdminService;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Resource
    AdminService adminService;

    // 查询所有身份
    @GetMapping("/getAllRole")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getAllRole() {
        return adminService.getAllRole();
    }

    // 查询用户数量
    @PostMapping("/getUserCount")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getUserCount(@RequestBody AdminGetUserCountRequest adminGetUserCountRequest) {
        return adminService.getUserCount(adminGetUserCountRequest);
    }

    // 查询所有用户
    @PostMapping("/getAllUser")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getAllUser(@RequestBody AdminGetAllUserRequest adminGetAllUserRequest) {
        return adminService.getAllUser(adminGetAllUserRequest);
    }

    // 通过id查询用户信息
    @GetMapping("/getUserInfoById")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getUserInfoById(@RequestParam String userId) {
        return adminService.getUserInfoById(userId);
    }

    // 通过id修改用户信息
    @PostMapping("/updateUserInfoById")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateUserInfoById(@RequestBody AdminUpdateUserInfoByIdRequest adminUpdateUserInfoByIdRequest) {
        return adminService.updateUserInfoById(adminUpdateUserInfoByIdRequest);
    }

    @GetMapping("/getCompetitionTypeCount")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getCompetitionTypeCount() {
        return adminService.getCompetitionTypeCount();
    }

    @GetMapping("/getAllCompetitionType")
    @PermitAll
    public Response getAllCompetitionType(@RequestParam int pageNow, @RequestParam int pageSize) {
        return adminService.getAllCompetitionType(pageNow, pageSize);
    }

    @GetMapping("/addCompetitionType")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response addCompetitionType(@RequestParam String typeName) {
        return adminService.addCompetitionType(typeName);
    }

    @GetMapping("/updateCompetitionType")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateCompetitionType(@RequestParam String id, @RequestParam String typeName) {
        return adminService.updateCompetitionType(id, typeName);
    }

    @GetMapping("/getCompetitionGroupCategoryByCompetitionId")
    @PermitAll
    public Response getCompetitionGroupCategoryByCompetitionId(@RequestParam String competitionId) {
        return adminService.getCompetitionGroupCategoryByCompetitionId(competitionId);
    }

    @PostMapping("/addCompetitionGroupCategory")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response addCompetitionGroupCategory(@RequestParam String competitionId, @RequestParam String name) {
        return adminService.addCompetitionGroupCategory(competitionId, name);
    }

    @PostMapping("/updateCompetitionGroupCategory")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateCompetitionGroupCategory(@RequestParam String id, @RequestParam String name) {
        return adminService.updateCompetitionGroupCategory(id, name);
    }

    @PostMapping("/getAllCompetition")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getAllCompetition(@RequestBody AdminGetAllCompetitionRequest adminGetAllCompetitionRequest) {
        return adminService.getAllCompetition(adminGetAllCompetitionRequest);
    }

    @PostMapping("/getCompetitionCount")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getCompetitionCount(@RequestBody AdminGetCompetitionCountRequest adminGetCompetitionCountRequest) {
        return adminService.getCompetitionCount(adminGetCompetitionCountRequest);
    }

    @GetMapping("/getCompetitionInfoById")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getCompetitionInfoById(@RequestParam String competitionId) {
        return adminService.getCompetitionInfoById(competitionId);
    }

    @PostMapping("/updateCompetitionInfoById")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateCompetitionInfoById(@RequestBody AdminUpdateCompetitionInfoByIdRequest adminUpdateCompetitionInfoByIdRequest) {
        return adminService.updateCompetitionInfoById(adminUpdateCompetitionInfoByIdRequest);
    }

    @PostMapping("/addCompetition")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response addCompetition(@Valid @RequestBody AdminAddCompetitionRequest adminAddCompetitionRequest) {
        return adminService.addCompetition(adminAddCompetitionRequest);
    }

    @GetMapping("/getAllCompetitionAdminBySchool")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getAllCompetitionAdminBySchool(@RequestParam String schoolId) {
        return adminService.getAllCompetitionAdminBySchool(schoolId);
    }

    @PostMapping("/getAllSchool")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getAllSchool(@RequestBody AdminGetAllSchoolRequest adminGetAllSchoolRequest) {
        return adminService.getAllSchool(adminGetAllSchoolRequest);
    }

    @PostMapping("/getSchoolCount")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getSchoolCount(@RequestBody AdminGetSchoolCountRequest adminGetSchoolCountRequest) {
        return adminService.getSchoolCount(adminGetSchoolCountRequest);
    }

    @GetMapping("/getSchoolInfoById")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getSchoolInfoById(@RequestParam String schoolId) {
        return adminService.getSchoolInfoById(schoolId);
    }

    @PostMapping("/updateSchoolInfoById")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateSchoolInfoById(@RequestBody AdminUpdateSchoolInfoByIdRequest adminUpdateSchoolInfoByIdRequest) {
        return adminService.updateSchoolInfoById(adminUpdateSchoolInfoByIdRequest);
    }

    @PostMapping("/addSchool")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response addSchool(@RequestBody AdminAddSchoolRequest adminAddSchoolRequest) {
        return adminService.addSchool(adminAddSchoolRequest);
    }

    @GetMapping("/getAllCoachBySchool")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getAllCoachBySchool(@RequestParam String schoolId) {
        return adminService.getAllCoachBySchool(schoolId);
    }

    @GetMapping("/getCurrentSeason")
    @PermitAll
    public Response getCurrentSeason() {
        return adminService.getCurrentSeason();
    }

    @GetMapping("/getSeasonCount")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getSeasonCount() {
        return adminService.getSeasonCount();
    }

    @GetMapping("/getAllSeason")
    @PermitAll
    public Response getAllSeason(@RequestParam int pageNow, @RequestParam int pageSize) {
        return adminService.getAllSeason(pageNow, pageSize);
    }

    @GetMapping("/setCurrentSeason")
    @PermitAll
    public Response setCurrentSeason(@RequestParam String seasonId) {
        return adminService.setCurrentSeason(seasonId);
    }

    @GetMapping("/addSeason")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response addSeason(@RequestParam String seasonId, @RequestParam Boolean isCurrentSeason) {
        return adminService.addSeason(seasonId, isCurrentSeason);
    }

    @GetMapping("/updateSeason")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateSeason(@RequestParam String newSeasonId, @RequestParam String oldSeasonId) {
        return adminService.updateSeason(newSeasonId, oldSeasonId);
    }

    @GetMapping("/getSystemInfo")
    @PermitAll
    public Response getSystemInfo() {
        return adminService.getSystemInfo();
    }

    @PostMapping("/updateSystemInfo")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateSystemInfo(@RequestBody AdminUpdateSystemInfoRequest adminUpdateSystemInfoRequest) {
        return adminService.updateSystemInfo(adminUpdateSystemInfoRequest);
    }

    @GetMapping("/getAllSlideshow")
    @PermitAll
    public Response getAllSlideshow(@RequestParam int pageNow, @RequestParam int pageSize) {
        return adminService.getAllSlideshow(pageNow, pageSize);
    }

    @PostMapping("/addSlideshow")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response addSlideshow(@RequestBody AdminAddSlideshowRequest adminAddSlideshowRequest) {
        return adminService.addSlideshow(adminAddSlideshowRequest);
    }

    @PostMapping("/updateSlideshow")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateSlideshow(@RequestBody AdminUpdateSlideshowRequest adminUpdateSlideshowRequest) {
        return adminService.updateSlideshow(adminUpdateSlideshowRequest);
    }

    @GetMapping("/deleteSlideshow")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response deleteSlideshow(@RequestParam String slideshowId) {
        return adminService.deleteSlideshow(slideshowId);
    }

    @PostMapping("/getAllAnnouncement")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getAllAnnouncement(@RequestBody AdminGetAllAnnouncementRequest adminGetAllAnnouncementRequest) {
        return adminService.getAllAnnouncement(adminGetAllAnnouncementRequest);
    }

    @GetMapping("/getAnnouncementInfo")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getAnnouncementInfo(@RequestParam String announcementId) {
        return adminService.getAnnouncementInfo(announcementId);
    }

    @PostMapping("/addAnnouncement")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response addAnnouncement(@RequestBody AdminAddAnnouncementRequest adminAddAnnouncementRequest) {
        return adminService.addAnnouncement(adminAddAnnouncementRequest);
    }

    @PostMapping("/updateAnnouncement")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateAnnouncement(@RequestBody AdminUpdateAnnouncementRequest adminUpdateAnnouncementRequest) {
        return adminService.updateAnnouncement(adminUpdateAnnouncementRequest);
    }

    @PostMapping("/getAllSponsor")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response getAllSponsor(@RequestBody AdminGetAllSponsorRequest adminGetAllSponsorRequest) {
        return adminService.getAllSponsor(adminGetAllSponsorRequest);
    }

    @PostMapping("/addSponsor")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response addSponsor(@RequestBody Sponsor sponsor) {
        return adminService.addSponsor(sponsor);
    }

    @PostMapping("/updateSponsor")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response updateSponsor(@RequestBody Sponsor sponsor) {
        return adminService.updateSponsor(sponsor);
    }

    @GetMapping("/getTestExcelFile")
    @PreAuthorize("hasAnyAuthority('admin')")
    public void getTestExcelFile(HttpServletResponse response){
        adminService.getTestExcelFile(response);
    }

    @PostMapping("/uploadTestFile")
    @PreAuthorize("hasAnyAuthority('admin')")
    public Response uploadTestFile(@RequestParam("file") MultipartFile file, @RequestParam String param) {
        return adminService.uploadTestFile(file, param);
    }
}
