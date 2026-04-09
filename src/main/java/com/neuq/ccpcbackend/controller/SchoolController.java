package com.neuq.ccpcbackend.controller;

import com.neuq.ccpcbackend.dto.SchoolUpdateInfoRequest;
import com.neuq.ccpcbackend.service.SchoolService;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/school")
public class SchoolController {

    @Resource
    SchoolService schoolService;

    @PostMapping("/updateInfo")
    @PreAuthorize("hasAnyAuthority('admin', 'schoolAdmin')")
    public Response updateInfo(@Valid @RequestBody SchoolUpdateInfoRequest schoolUpdateInfoRequest) {
        return schoolService.updateInfo(schoolUpdateInfoRequest);
    }

    @GetMapping("/getInfo")
    @PreAuthorize("hasAnyAuthority('admin', 'schoolAdmin')")
    public Response getInfo() {
        return schoolService.getInfo();
    }

    @GetMapping("/generateSchoolInviteToken")
    @PreAuthorize("hasAnyAuthority('schoolAdmin')")
    public Response generateSchoolInviteToken(@RequestParam Long seconds) {
        return schoolService.generateSchoolInviteToken(seconds);
    }

    @GetMapping("/acceptSchoolInvite")
    @PreAuthorize("hasAnyAuthority('coach')")
    public Response acceptSchoolInvite(@RequestParam String token) {
        return schoolService.acceptSchoolInvite(token);
    }
}
