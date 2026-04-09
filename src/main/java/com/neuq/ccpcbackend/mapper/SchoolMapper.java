package com.neuq.ccpcbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuq.ccpcbackend.dto.ExportSchoolsEmblem;
import com.neuq.ccpcbackend.entity.School;
import com.neuq.ccpcbackend.vo.CompetingSchoolsExportVo;
import com.neuq.ccpcbackend.vo.SchoolInvoiceInfoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SchoolMapper extends BaseMapper<School> {
    List<CompetingSchoolsExportVo> getIdByCompetitionId(String competitionId);

    List<ExportSchoolsEmblem> selectCompetingSchoolsEmblem(String competitionId);

    List<SchoolInvoiceInfoVO> selectInvoiceInfoByCompetitionId(String competitionId);
}
