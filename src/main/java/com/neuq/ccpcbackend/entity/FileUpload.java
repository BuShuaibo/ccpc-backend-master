package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("fileupload")
public class FileUpload {
    //用户id
    public String userId;
    //文件id
    public String id;
    public String fileName;
    public String fileType;
    public String filePath;
    public Long fileSize;
    public String md5;
    public Long uploadTime;
}
