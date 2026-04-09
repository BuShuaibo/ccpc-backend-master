package com.neuq.ccpcbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuq.ccpcbackend.entity.FileUpload;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FileMapper extends BaseMapper<FileUpload> {
    @Select("SELECT file_path FROM fileupload WHERE md5 = #{md5}")
    String selectFileByMd5(@Param("md5") String md5);
}
