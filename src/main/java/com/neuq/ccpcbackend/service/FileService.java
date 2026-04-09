package com.neuq.ccpcbackend.service;

import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    Response upload(MultipartFile file);
    void download(String fileStorageName, HttpServletResponse response);
}
