package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.entity.PostImage;
import com.shijian.mapper.PostImageMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private static final long MAX_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    ));

    @Resource
    private PostImageMapper postImageMapper;

    @PostMapping
    public Result<?> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            return Result.error("文件大小不能超过10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return Result.error("不支持的文件类型");
        }
        try {
            PostImage img = new PostImage();
            img.setData(file.getBytes());
            img.setUrl(contentType);
            postImageMapper.insert(img);

            String imageUrl = "/api/image/" + img.getId();
            Map<String, String> result = new HashMap<>();
            result.put("url", imageUrl);
            result.put("name", file.getOriginalFilename());
            result.put("id", String.valueOf(img.getId()));
            return Result.success("上传成功", result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
