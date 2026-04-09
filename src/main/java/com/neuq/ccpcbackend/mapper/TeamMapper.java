package com.neuq.ccpcbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuq.ccpcbackend.entity.Team;
import com.neuq.ccpcbackend.entity.User;
import com.neuq.ccpcbackend.vo.TeamVo;
import com.neuq.ccpcbackend.vo.TeamExportVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TeamMapper extends BaseMapper<Team> {
    TeamVo getTeamInfo(String teamId);
    List<User> getMember(String teamId);
    List<TeamExportVo> getIdByCompetitionId(String competitionId);
    Long getMemberCount(int value);
}
