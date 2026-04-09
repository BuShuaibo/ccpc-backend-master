package com.neuq.ccpcbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuq.ccpcbackend.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
}
