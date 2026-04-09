package com.neuq.ccpcbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuq.ccpcbackend.dto.CoachInfoResponse;
import com.neuq.ccpcbackend.entity.Signup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SignupMapper extends BaseMapper<Signup>{
    // 获取同一学校的所有教练
    List<CoachInfoResponse> getSchoolCoaches(@Param("schoolId") String schoolId);

    List<Signup> getSignupByCoachId(@Param("coachId")String coachId, @Param("offset") int offset,@Param("pageSize") int pageSize,@Param("coachName") String coachName,@Param("competition") String competition,@Param("teamName") String teamName,@Param("processStatus") Integer processStatus,@Param("occupationQuotaType") Integer occupationQuotaType ,@Param("memberNames") List<String> teamMembers);

    Integer countSignupByCoachId(@Param("competition") String competition,
                                 @Param("teamName") String teamName,
                                 @Param("processStatus") Integer processStatus,
                                 @Param("occupationQuotaType") Integer occupationQuotaType,
                                 @Param("coachName") String coachName,
                                 @Param("memberNames") List<String> memberNames,
                                 @Param("coachId") String coachId
    );
}
