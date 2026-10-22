package com.shijian.controller;

import com.shijian.entity.PostImage;
import com.shijian.mapper.PostImageMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    @Resource
    private PostImageMapper postImageMapper;

    @GetMapping("/{id}")
    public void getImage(@PathVariable Long id, HttpServletResponse response) throws IOException {
        PostImage img = postImageMapper.selectById(id);
        if (img == null || img.getData() == null || img.getData().length == 0) {
            response.setStatus(404);
            return;
        }
        // 根据内容嗅探 MIME 类型
        byte[] data = img.getData();
        String contentType = sniffMime(data);
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age=86400");
        OutputStream out = response.getOutputStream();
        out.write(data);
        out.flush();
    }

    private static String sniffMime(byte[] data) {
        if (data.length < 4) return MediaType.IMAGE_JPEG_VALUE;
        int b0 = data[0] & 0xFF;
        int b1 = data[1] & 0xFF;
        int b2 = data[2] & 0xFF;
        int b3 = data[3] & 0xFF;
        if (b0 == 0xFF && b1 == 0xD8) return MediaType.IMAGE_JPEG_VALUE;
        if (b0 == 0x89 && b1 == 0x50 && b2 == 0x4E && b3 == 0x47) return MediaType.IMAGE_PNG_VALUE;
        if (b0 == 0x47 && b1 == 0x49 && b2 == 0x46 && b3 == 0x38) return MediaType.IMAGE_GIF_VALUE;
        if (b0 == 0x52 && b1 == 0x49 && b2 == 0x46 && b3 == 0x46) return "image/webp";
        return MediaType.IMAGE_JPEG_VALUE;
    }
}
