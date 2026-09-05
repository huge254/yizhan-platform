package com.yizhan.backend.controller;

import com.yizhan.backend.common.Result;
import com.yizhan.backend.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /** 图片上传，返回可直接用于 <img src> 的访问路径 */
    @PostMapping("/api/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        return Result.success(Map.of("url", url));
    }
}
