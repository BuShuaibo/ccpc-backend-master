package com.neuq.ccpcbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuq.ccpcbackend.entity.SignupMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SignupMemberMapper extends BaseMapper<SignupMember> {
}
