package com.neuq.ccpcbackend.controller;

import com.neuq.ccpcbackend.service.FileService;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Transactional
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Resource
    FileService fileService;

    @PostMapping("/upload")
    @PermitAll
    public Response upload(@RequestBody MultipartFile file) {
        return fileService.upload(file);
    }

    @GetMapping("/{fileStorageName}")
    @PermitAll
    public void download(@PathVariable("fileStorageName")String fileStorageName, HttpServletResponse response) {
        fileService.download(fileStorageName, response);
    }
}
