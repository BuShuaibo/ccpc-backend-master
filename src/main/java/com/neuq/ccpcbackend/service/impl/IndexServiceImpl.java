package com.neuq.ccpcbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuq.ccpcbackend.dto.AnnouncementContentResponse;
import com.neuq.ccpcbackend.dto.UserGetAnnouncementRequest;
import com.neuq.ccpcbackend.dto.UserGetAnnouncement;
import com.neuq.ccpcbackend.dto.UserGetAnnouncementResponse;
import com.neuq.ccpcbackend.entity.*;
import com.neuq.ccpcbackend.entity.Announcement;
import com.neuq.ccpcbackend.entity.Competition;
import com.neuq.ccpcbackend.entity.Sponsor;
import com.neuq.ccpcbackend.entity.SystemInfo;
import com.neuq.ccpcbackend.entity.User;
import com.neuq.ccpcbackend.mapper.*;
import com.neuq.ccpcbackend.service.IndexService;
import com.neuq.ccpcbackend.utils.LambdaQueryWrapperExtension;
import com.neuq.ccpcbackend.utils.response.Response;
import com.neuq.ccpcbackend.vo.IndexSponsorVo;
import jakarta.annotation.Resource;
import lombok.experimental.ExtensionMethod;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Service
@ExtensionMethod({LambdaQueryWrapperExtension.class, LambdaQueryWrapper.class})
public class IndexServiceImpl implements IndexService {

    @Resource
    private CompetitionMapper competitionMapper;

    @Resource
    private AnnouncementMapper announcementMapper;
    @Resource
    private SponsorMapper sponsorMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private SystemInfoMapper systemInfoMapper;
    @Resource
    private SeasonMapper seasonMapper;


    @Override
    public Response getTopCompetition() {
        LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.select(Competition::getId,Competition::getName,Competition::getStartTime).orderByDesc(Competition::getStartTime).last("limit 5");
        List<Competition> competitions = competitionMapper.selectList(queryWrapper);
        return Response.of(competitions);
    }

    @Override
    public Response getTopAnnouncement() {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.select(Announcement::getId, Announcement::getTitle, Announcement::getUpdateAt).orderByDesc(Announcement::getUpdateAt).last("limit 5");
        List<Announcement> announcements = announcementMapper.selectList(queryWrapper);
        return Response.of(announcements);
    }

    @Override
    public Response getAnnouncementInfo(String id) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Announcement::getId,id);
        Announcement announcement = announcementMapper.selectOne(queryWrapper);
        //浏览量加一
        if(!id.equals("介绍")){
            LambdaUpdateWrapper<Announcement> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Announcement::getId,id)
                        .setSql("view_count = view_count + 1");
            announcementMapper.update(null,updateWrapper);
        }
        return Response.of(announcement);
    }

    @Override
    public Response getHomeSystemInfo() {
        SystemInfo systemInfo = systemInfoMapper.selectById("1");
        return Response.of(systemInfo);
    }

    @Override
    public Response getAllSponsor() {
        //白金赞助商
        LambdaQueryWrapper<Sponsor> queryWrapperPlatinum = new LambdaQueryWrapper();
        queryWrapperPlatinum.eq(Sponsor::getType,1).eq(Sponsor::getShow,1);
        List<Sponsor> platinumSponsors = sponsorMapper.selectList(queryWrapperPlatinum);
        //普通赞助商
        LambdaQueryWrapper<Sponsor> queryWrapperNormal = new LambdaQueryWrapper();
        queryWrapperNormal.eq(Sponsor::getType,2).eq(Sponsor::getShow,1);
        List<Sponsor> normalSponsors = sponsorMapper.selectList(queryWrapperNormal);
        //支持院校
        LambdaQueryWrapper<Sponsor> queryWrapperSchool = new LambdaQueryWrapper();
        queryWrapperSchool.eq(Sponsor::getType,3).eq(Sponsor::getShow,1);
        List<Sponsor> schools = sponsorMapper.selectList(queryWrapperSchool);

        IndexSponsorVo sponsorVo = new IndexSponsorVo();
        sponsorVo.setPlatinumSponsors(platinumSponsors);
        sponsorVo.setNormalSponsors(normalSponsors);
        sponsorVo.setSchools(schools);
        return Response.of(sponsorVo);
    }


    @Override
    public Response getAllAnnouncement(UserGetAnnouncementRequest userGetAnnouncementRequest) {
        //分页条件
        int nowPage = userGetAnnouncementRequest.getPageNow();
        int pageSize = userGetAnnouncementRequest.getPageSize();
        Page<Announcement> page=new Page<>(nowPage, pageSize);
        //查询条件
        String title=userGetAnnouncementRequest.getTitle();
        String authorName=userGetAnnouncementRequest.getAuthorName();
        String season=userGetAnnouncementRequest.getSeason();
        if(season==null||season.isEmpty()){
            //默认当前赛季
            LambdaQueryWrapper<Season> sQuery = new LambdaQueryWrapper<>();
            sQuery.eq(Season::getIsCurrentSeason,1);
            sQuery.select(Season::getId);
            season=seasonMapper.selectOne(sQuery).getId();
        }
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Announcement::getStatus,1);
        queryWrapper.like(title!=null&& !title.isEmpty(),Announcement::getTitle,title);
        queryWrapper.like(Announcement::getSeason,season);
        queryWrapper.select(Announcement::getId,Announcement::getTitle,Announcement::getCoverPictureUrl,
                Announcement::getCreateAt,Announcement::getUpdateAt,Announcement::getAuthorId
        ,Announcement::getSummary,Announcement::getSeason,Announcement::getViewCount);


        if(authorName!=null&& !authorName.isEmpty()){
            List<String> authorId=userMapper.getIdByName(authorName);
            if(authorId!=null&& !authorId.isEmpty()) {
                queryWrapper.in(Announcement::getAuthorId, authorId);
            }
        }
        //默认按更新时间进行排序,先显示更新时间更靠近现在的
        queryWrapper.orderByDesc(Announcement::getUpdateAt);

        //分页查询
        Page<Announcement> announcements = announcementMapper.selectPage(page,queryWrapper);
        //封装
        UserGetAnnouncementResponse userGetAnnouncementResponse = new UserGetAnnouncementResponse();
        List<Announcement>records=page.getRecords();
        if(records.isEmpty()){
            userGetAnnouncementResponse.setUserGetAnnouncements(Collections.emptyList());
        }else{
            ArrayList<UserGetAnnouncement>arrayList=new ArrayList<>();
            for(Announcement record:records){
                UserGetAnnouncement temp=new UserGetAnnouncement();
                temp.setId(record.getId());
                temp.setTitle(record.getTitle());
                temp.setSummary(record.getSummary());
                User u1=userMapper.selectById(record.getAuthorId());
                temp.setAuthorName(u1.getFirstName()+u1.getLastName());
                temp.setCover_picture_url(record.getCoverPictureUrl());
                temp.setUpdateTime(record.getUpdateAt());
                temp.setViewCount(record.getViewCount());
                arrayList.add(temp);
            }
            //List<UserGetAnnouncement> ans= BeanUtil.copyToList(records, UserGetAnnouncement.class);

            userGetAnnouncementResponse.setUserGetAnnouncements(arrayList);
        }
        userGetAnnouncementResponse.setTotalCount(announcements.getTotal());
        return Response.of(userGetAnnouncementResponse);
    }

    @Override
    public Response getAnnouncementContent(String id) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Announcement::getId, id);

        Announcement announcement = announcementMapper.selectOne(queryWrapper);
        if (announcement == null) {
            return Response.error("404", "Announcement not found");
        }

        AnnouncementContentResponse response = new AnnouncementContentResponse();
        response.setId(announcement.getId());
        response.setCover_picture_url(announcement.getCoverPictureUrl());

        // Fetch author name from User table
        String authorName = announcement.getAuthorId();
        if (announcement.getAuthorId() != null && !announcement.getAuthorId().isEmpty()) {
            User author = userMapper.selectById(announcement.getAuthorId());
            if (author != null) {
                // Use the author's name if available
                authorName = author.getId(); // Default to ID if name not available
                if (author.getFirstName() != null && !author.getFirstName().isEmpty() && 
                    author.getLastName() != null && !author.getLastName().isEmpty()) {
                    authorName = author.getFirstName() + " " + author.getLastName();
                } else if (author.getFirstName() != null && !author.getFirstName().isEmpty()) {
                    authorName = author.getFirstName();
                } else if (author.getLastName() != null && !author.getLastName().isEmpty()) {
                    authorName = author.getLastName();
                }
            }
        }
        response.setAuthorName(authorName);

        response.setUpdateTime(announcement.getUpdateAt());
        response.setTitle(announcement.getTitle());
        response.setSummary(announcement.getSummary());
        response.setViewCount(announcement.getViewCount());
        response.setContent(announcement.getContent());

        return Response.of(response);
    }
}
