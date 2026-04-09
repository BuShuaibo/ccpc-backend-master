package com.neuq.ccpcbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuq.ccpcbackend.dto.UserKeywordRequest;
import com.neuq.ccpcbackend.entity.User;
import com.neuq.ccpcbackend.vo.SchoolGetAllCoachVo;
import com.neuq.ccpcbackend.vo.SchoolGetAllCompetitionAdminVo;
import com.neuq.ccpcbackend.vo.UserGetCoachAndSchoolVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<String> getAllRoleByUserId(String userId);
    UserGetCoachAndSchoolVo getCoachAndSchoolByUserId(String userId);
    List<SchoolGetAllCompetitionAdminVo> getAllCompetitionAdminBySchool(String schoolId);
    Page<User> getAllSchoolCoach(Page<User> page, @Param("schoolId") String schoolId, @Param("userKeywordRequest") UserKeywordRequest userKeywordRequest);
    List<SchoolGetAllCoachVo> getAllCoachBySchool(String schoolId);
    List<String> getIdByName(String name);
}
