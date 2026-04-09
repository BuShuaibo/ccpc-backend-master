package com.neuq.ccpcbackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.neuq.ccpcbackend.entity.FileUpload;
import com.neuq.ccpcbackend.mapper.FileMapper;
import com.neuq.ccpcbackend.service.FileService;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.response.Response;
import com.neuq.ccpcbackend.utils.RoleUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Resource
    FileMapper fileMapper;

    //文件存储路径
    @Value("${file.upload.path}")
    String fileUploadPath;

    //文件访问url前缀
    @Value("${file.url.prefix}")
    String fileUrlPrefix;

    //一个随机数加上文件名加文件类型组成该文件的唯一标识 fileStorageName ，最后返回该文件的存储路径
    //部署时把配置文件里的 file.upload.path 和 file.url.prefix 更改为服务器相关参数即可
    @Override
    public Response upload(MultipartFile file) {
        String url;
        String fileName = file.getOriginalFilename();
        String fileType = FileUtil.extName(fileName);
        String userid = RoleUtil.getCurrentUserId();
        long size = file.getSize();
        String fileStorageName = UUID.randomUUID() + StrUtil.DOT + fileType;
        File uploadFile = new File(fileUploadPath + fileStorageName);
        if(!uploadFile.getParentFile().exists()){
            uploadFile.getParentFile().mkdirs();
        }
        try {
            file.transferTo(uploadFile);
        } catch (IOException e) {
            return Response.error(ErrorCode.INSERT_FAILED.getErrCode(), "上传文件失败");
        }
        String md5 = SecureUtil.md5(uploadFile);
        String path = fileMapper.selectFileByMd5(md5);
        Long uploadTime = System.currentTimeMillis();
        if(path!=null){
            url = path;
            uploadFile.delete();
        }else {
            url = fileUrlPrefix + fileStorageName;
            FileUpload fileUpload = new FileUpload();
            fileUpload.setId(UUID.randomUUID().toString());
            fileUpload.setUserId(userid);
            fileUpload.setFileName(fileName);
            fileUpload.setFileType(fileType);
            fileUpload.setFileSize(size/1024);
            fileUpload.setFilePath(url);
            fileUpload.setMd5(md5);
            fileUpload.setUploadTime(uploadTime);
            try {
                fileMapper.insert(fileUpload);
            } catch (Exception e){
                e.printStackTrace();
                return Response.error(ErrorCode.INSERT_FAILED.getErrCode(), "上传文件失败");
            }
        }
        return Response.of(url);
    }

    @Override
    public void download(String fileStorageName, HttpServletResponse response) {
        File file = new File(fileUploadPath + fileStorageName);
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(fileStorageName, StandardCharsets.UTF_8));
            outputStream.write(FileUtil.readBytes(file));
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
