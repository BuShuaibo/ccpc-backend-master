package com.neuq.ccpcbackend.controller;

import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.entity.Team;
import com.neuq.ccpcbackend.entity.User;
import com.neuq.ccpcbackend.service.UserService;
import com.neuq.ccpcbackend.utils.RoleUtil;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    UserService userService;

    @GetMapping("/helloWorld")
    @PreAuthorize("hasAuthority('admin')")
    public Response helloWorld() {
        return Response.of("Hello World");
    }

    @PostMapping("/loginWithCode")
    @PermitAll
    public Response loginWithCode(@Valid @RequestBody UserLoginWithCodeRequest userLoginWithCodeRequest) {
        return userService.loginWithCode(userLoginWithCodeRequest);
    }

    @PostMapping("/loginWithPassword")
    @PermitAll
    public Response loginWithPassword(@Valid @RequestBody UserLoginWithPasswordRequest userLoginWithPasswordRequest) {
        return userService.loginWithPassword(userLoginWithPasswordRequest);
    }

    @PostMapping("/register")
    @PermitAll
    public Response register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        return userService.register(userRegisterRequest);
    }

    @GetMapping("/refreshToken")
    @PermitAll
    public Response refreshToken(@RequestParam String refreshToken) {
        return userService.refreshToken(refreshToken);
    }

    @GetMapping("/getCode")
    @PermitAll
    public Response getCode(@RequestParam String phone) {
        return userService.getCode(phone);
    }

    @PostMapping("/updateBaseInfo")
    @PreAuthorize("hasAnyAuthority('user', 'admin', 'contest', 'coach', 'schoolAdmin', 'contestAdmin')")
    public Response updateBaseInfo(@Valid @RequestBody UserUpdateBaseInfoRequest userUpdateBaseInfoRequest) {
        return userService.updateBaseInfo(userUpdateBaseInfoRequest);
    }

    @GetMapping("/getBaseInfo")
    @PreAuthorize("hasAnyAuthority('user', 'admin', 'contest', 'coach', 'schoolAdmin', 'contestAdmin')")
    public Response getBaseInfo() {
        return userService.getBaseInfo();
    }

    @PostMapping("/updateAddressInfo")
    @PreAuthorize("hasAnyAuthority('user', 'admin', 'contest', 'coach', 'schoolAdmin', 'contestAdmin')")
    public Response updateAddressInfo(@Valid @RequestBody UserUpdateAddressInfoRequest userUpdateAddressInfoRequest) {
        return userService.updateAddressInfo(userUpdateAddressInfoRequest);
    }

    @GetMapping("/getAddressInfo")
    @PreAuthorize("hasAnyAuthority('user', 'admin', 'contest', 'coach', 'schoolAdmin', 'contestAdmin')")
    public Response getAddressInfo() {
        return userService.getAddressInfo();
    }

    @PostMapping("/updatePasswordInfo")
    @PreAuthorize("hasAnyAuthority('user', 'admin', 'contest', 'coach', 'schoolAdmin', 'contestAdmin')")
    public Response updatePasswordInfo(@Valid @RequestBody UserUpdatePasswordInfoRequest userUpdatePasswordInfoRequest) {
        return userService.updatePasswordInfo(userUpdatePasswordInfoRequest);
    }

    @GetMapping("/getPasswordInfo")
    @PreAuthorize("hasAnyAuthority('user', 'admin', 'contest', 'coach', 'schoolAdmin', 'contestAdmin')")
    public Response getPasswordInfo() {
        return userService.getPasswordInfo();
    }





    // 教练查询所有自己麾下的学生
    @PostMapping("/getAllCoachStudent")
    @PreAuthorize("hasAnyAuthority('admin', 'coach')")
    public Response getAllCoachStudent(@RequestBody StudentQueryRequest studentQueryRequest) {
        return userService.getAllCoachStudent(studentQueryRequest);
    }

    //教练查询麾下学生数量
    @PostMapping("/getCoachStudentCount")
    @PreAuthorize("hasAnyAuthority('admin', 'coach')")
    public Response getCoachStudentCount(@RequestBody StudentQueryRequest studentQueryRequest) {
        return userService.getCoachStudentCount(studentQueryRequest);
    }

    // 学校管理员查询所有自己学校的学生
    @GetMapping("/getAllSchoolStudent")
    @PreAuthorize("hasAnyAuthority('admin', 'schoolAdmin')")
    public Response getAllSchoolStudent(@RequestParam int pageNow, @RequestParam int pageSize,
                                        @RequestParam String sortType, @RequestParam String keyword) {
        return userService.getAllSchoolStudent(pageNow, pageSize, sortType, keyword);
    }

    @GetMapping("/getSchoolStudentCount")
    @PreAuthorize("hasAnyAuthority('admin', 'schoolAdmin')")
    public Response getSchoolStudentCount(@RequestParam String keyword) {
        return userService.getSchoolStudentCount(keyword);
    }

    @GetMapping("/generateCoachInviteToken")
    @PreAuthorize("hasAnyAuthority('coach')")
    public Response generateCoachInviteToken(@RequestParam Long seconds) {
        return userService.generateCoachInviteToken(seconds);
    }

    @GetMapping("/acceptCoachInvite")
    @PreAuthorize("hasAnyAuthority('contest')")
    public Response acceptCoachInvite(@RequestParam String token) {
        return userService.acceptCoachInvite(token);
    }

    @GetMapping("/parseInviteToken")
    @PermitAll
    public Response parseInviteToken(@RequestParam String token) {
        return userService.parseInviteToken(token);
    }

    @GetMapping("/getAllInviteToken")
    @PreAuthorize("hasAnyAuthority('contest', 'coach', 'schoolAdmin')")
    public Response getAllInviteToken() {
        return userService.getAllInviteToken();
    }


    @GetMapping("/getCoachStudent/{id}")
    @PreAuthorize("hasAnyAuthority('admin','coach')")
    public Response getCoachStudent(@PathVariable String id) {
        return userService.getCoachStudent(id);
    }

    @PostMapping("/updateCoachStudent")
    @PreAuthorize("hasAnyAuthority('admin','coach')")
    public Response updateCoachStudent(@RequestBody CoachUpdateStudentRequest coachUpdateStudentRequest) {
        return userService.updateCoachStudent(coachUpdateStudentRequest);
    }

}
