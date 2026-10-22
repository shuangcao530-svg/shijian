package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.entity.Category;
import com.shijian.mapper.CategoryMapper;
import com.shijian.service.AdminService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Resource
    private AdminService adminService;

    @Resource
    private CategoryMapper categoryMapper;

    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminService.listUsers(keyword, page, size));
    }

    @PutMapping("/users/{userId}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long userId) {
        return adminService.toggleUserStatus(userId);
    }

    @GetMapping("/posts/pending")
    public Result<?> listPendingPosts(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminService.listPendingPosts(page, size));
    }

    @PutMapping("/posts/{postId}/audit")
    public Result<Void> auditPost(@PathVariable Long postId, @RequestParam Integer status) {
        return adminService.auditPost(postId, status);
    }

    @PostMapping("/category")
    public Result<?> addCategory(@RequestBody Category category) {
        return adminService.addCategory(category);
    }

    @PutMapping("/category/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return adminService.updateCategory(id, category);
    }

    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        return adminService.deleteCategory(id);
    }

    @GetMapping("/categories")
    public Result<?> listCategories() {
        return Result.success(categoryMapper.selectList(null));
    }

    @GetMapping("/reports")
    public Result<?> listReports(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminService.listReports(page, size));
    }

    @PutMapping("/reports/{reportId}")
    public Result<Void> handleReport(@PathVariable Long reportId,
                                     @RequestParam String result,
                                     @RequestParam(required = false) String handleResult,
                                     HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        return adminService.handleReport(adminId, reportId, result, handleResult);
    }

    @GetMapping("/stats")
    public Result<?> getStats() {
        return Result.success(adminService.getStats());
    }
}
