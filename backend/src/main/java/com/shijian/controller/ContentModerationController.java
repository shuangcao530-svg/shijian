package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.service.ContentModerationService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/moderation")
public class ContentModerationController {
    @Resource
    private ContentModerationService contentModerationService;

    @PostMapping("/check")
    public Result<?> checkContent(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isEmpty()) {
            return Result.error("内容不能为空");
        }
        return Result.success(contentModerationService.moderateText(text));
    }

    @GetMapping("/sensitive-words")
    public Result<?> listSensitiveWords() {
        return Result.success(contentModerationService.getSensitiveWords());
    }

    @PostMapping("/sensitive-words")
    public Result<?> addSensitiveWord(@RequestBody Map<String, Object> body) {
        String word = (String) body.get("word");
        String category = (String) body.getOrDefault("category", "GENERAL");
        int severity = body.get("severity") != null ? ((Number) body.get("severity")).intValue() : 1;
        contentModerationService.addSensitiveWord(word, category, severity);
        return Result.success("添加成功", null);
    }

    @DeleteMapping("/sensitive-words/{id}")
    public Result<?> removeSensitiveWord(@PathVariable Long id) {
        contentModerationService.removeSensitiveWord(id);
        return Result.success("删除成功", null);
    }
}
