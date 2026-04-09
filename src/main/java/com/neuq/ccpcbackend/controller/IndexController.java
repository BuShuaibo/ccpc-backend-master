package com.neuq.ccpcbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuq.ccpcbackend.dto.UserGetAnnouncementRequest;
import com.neuq.ccpcbackend.service.IndexService;
import com.neuq.ccpcbackend.utils.LambdaQueryWrapperExtension;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.experimental.ExtensionMethod;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@ExtensionMethod({LambdaQueryWrapperExtension.class, LambdaQueryWrapper.class})
@RestController
@RequestMapping("/api/index")
public class IndexController {

    @Resource
    private IndexService indexService;

    //首页简单展示的几个比赛
    @GetMapping("/getTopCompetition")
    @PermitAll()
    public Response getTopCompetition() {
        return indexService.getTopCompetition();
    }
    //首页展示的几个新闻
    @GetMapping("/getTopAnnouncement")
    @PermitAll()
    public Response getTopAnnouncement() {
        return indexService.getTopAnnouncement();
    }

    //获取公告信息
    @GetMapping("/getAnnouncementInfo")
    @PermitAll()
    public Response getAnnouncementInfo(@RequestParam String id) {
        return indexService.getAnnouncementInfo(id);
    }

    //首页4项数据展示
    @GetMapping("/getHomeSystemInfo")
    @PermitAll()
    public Response getHomeSystemInfo() {
        return indexService.getHomeSystemInfo();
    }

    //获取赞助商
    @GetMapping("/getAllSponsor")
    @PermitAll()
    public Response getAllSponsor() {
        return indexService.getAllSponsor();
    }


    //TODO改名 默认选择当前赛季
    //公告页面条件查询 分页查询公告
    @PostMapping("/getAllAnnouncement")
    @PermitAll()
    public Response getAllAnnouncement(@RequestBody UserGetAnnouncementRequest userGetAnnouncementRequest) {
        return indexService.getAllAnnouncement(userGetAnnouncementRequest);
    }

    //根据id获取公告内容
    @GetMapping("/getAnnouncementContent")
    @PermitAll()
    public Response getAnnouncementContent(@RequestParam String id) {
        return indexService.getAnnouncementContent(id);
    }
}
